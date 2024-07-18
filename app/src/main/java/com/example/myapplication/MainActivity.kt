package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.pages.HomePage
import com.example.myapplication.features.bitmaps.BitmapLoaderVm
import com.example.myapplication.features.bitmaps.BitmapUIEvent
import com.example.myapplication.features.bitmaps.BitmapUiState
import com.example.myapplication.services.ScreenRecordingService
import com.example.myapplication.ui.components.LoadingScreen

const val AGSL_SPACE = """
    uniform shader composable;
    const int iterations = 17;
const float formuparam = 0.53;

const int volsteps = 20;
const float stepsize = 0.1;

const float zoom = 0.800;
const float tile = 0.850;
const float speed = 0.010;

const float brightness = 0.0015;
const float darkmatter = 0.300;
const float distfading = 0.730;
const float saturation = 0.850;

uniform vec2 iSize;
uniform float iTime;
uniform vec2 iMouse;

half4 main(float2 fragCoord) {
    vec2 uv = fragCoord.xy / iSize.xy - 0.5;
    uv.y *= iSize.y / iSize.x;
    vec3 dir = vec3(uv * zoom, 1.0);
    float time = iTime * (speed * 0.25);

    float a1 = 0.5 + 0.5 / iSize.x * 2.0;    float a2 = 0.8 + 0.5 / iSize.y * 2.0;

    mat2 rot1 = mat2(cos(a1), sin(a1), -sin(a1), cos(a1));
    mat2 rot2 = mat2(cos(a2), sin(a2), -sin(a2), cos(a2));
    dir.xz *= rot1;
    dir.xy *= rot2;
    vec3 from = vec3(1.0, 0.5, 0.5);
    from += vec3(time * 2.0, time, -2.0);
    from.xz *= rot1;
    from.xy *= rot2;

    float s = 0.1, fade = 1.0;
    vec3 v = vec3(0.0);
    for (int r = 0; r < volsteps; r++) {
        vec3 p = from + s * dir * 0.5;
        p = abs(vec3(tile) - mod(p, vec3(tile * 2.0)));
        float pa, a = pa = 0.0;
        for (int i = 0; i < iterations; i++) {
            p = abs(p) / dot(p, p) - formuparam;
            a += abs(length(p) - pa);
            pa = length(p);
        }
        float dm = max(0.0, darkmatter - a * a * 0.001);
        a *= a * a;
        if (r > 6) fade *= 1.0 - dm;
        v += fade;
        v += vec3(s, s * s, s * s * s * s) * a * brightness * fade;
        fade *= distfading;
        s += stepsize;
    }
    
    v = mix(vec3(length(v)), v, saturation);
    return (composable.eval(v.xx) + composable.eval(v.yy) + composable.eval(v.zz));
    //return vec4(v * 0.01, 1.0);
}
"""

const val TAG: String = ""

class MainActivity : ComponentActivity() {

    private lateinit var vm: BitmapLoaderVm

    private lateinit var mediaProjectionManager: MediaProjectionManager

    private var screenRecordingService: ScreenRecordingService? = null

    private var mediaProjectionServiceIsBound = MutableLiveData<Boolean>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ScreenRecordingService.ScreenRecordingBinder
            screenRecordingService = binder.getService()
            mediaProjectionServiceIsBound.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            screenRecordingService = null
            mediaProjectionServiceIsBound.value = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val factory = BitmapLoaderVm.provideFactory(
                context = this,
                sessionToken = 0,
            )
            vm = viewModel(factory = factory)
            val bitmapsState by vm.state.collectAsState()
            Log.w("gustavdebug", "maincontent!")
            MainContent(bitmapsState, vm)
        }

        startMediaProjectionService()
        // request mediaprojection even if we dont immediately record, this is so we can start
        // the service (we don't record from the start)
        // Registers a photo picker activity launcher in single-select mode.
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker
                if (uri != null) {
                    vm.onEvent(BitmapUIEvent.LoadImageFromNativeImgGallery(uri))
                    Log.d("gustavdebug", "Selected URI: $uri")
                } else {
                    Log.d("gustavdebug", "No media selected")
                }
            }

        mediaProjectionServiceIsBound.observe(this) { bound ->
            if (bound && screenRecordingService != null) {
                vm.onAppWarmedUp(screenRecordingService!!)
                observeEventsFromViewModel(pickMedia)
            }
        }
    }

    private fun observeEventsFromViewModel(
        pickMedia: ActivityResultLauncher<PickVisualMediaRequest>,
    ) {
        vm.event.observe(this) {
            it.getContentIfNotHandled()?.let { event ->
                when (event) {
                    MainActivityEvent.AddUserPicture ->
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
        }
    }

    /**
     * request permission for screen recording and start service
     */
    private fun startMediaProjectionService() {
        mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
        val startMediaProjection = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val intent = Intent(this, ScreenRecordingService::class.java).apply {
                    putExtra("resultCode", result.resultCode)
                    putExtra("data", result.data)
                }
                ContextCompat.startForegroundService(this, intent)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }
        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    @Composable
    private fun MainContent(
        bitmapsState: BitmapUiState,
        vm: BitmapLoaderVm,
    ) {
        AppTheme {
            Surface {
                if (bitmapsState.isLoading) {
                    LoadingScreen()
                } else {
                    HomePage(
                        vm = vm,
                        bitmapsState = bitmapsState,
                    )
                }
            }
        }
    }
}

