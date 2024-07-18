package com.example.myapplication.data

const val AGSL_RIPPLE_EFFECT = """
    uniform shader composable;
    uniform float2 iSize;
    uniform float iTime;
    uniform float timeMultiplier;
    uniform float rippleMultiplier;
    uniform float zoomMultiplier;
    uniform float dirMultiplier;

    
    half4 main(float2 fragCoord) {
        float scale = 1/ iSize.x;
        float2 scaledCoord = fragCoord * scale * zoomMultiplier;
        float2 center = iSize * 0.5 * scale;
        float dist = distance(scaledCoord, center);
        float2 dir = scaledCoord - center * dirMultiplier;
        float speedFactor = 0.5;
       
        float distMultiplier = 10.0;
        float sin = sin(dist * rippleMultiplier + (iTime * timeMultiplier) * 6.28);
        float2 offset = dir * sin;
        float2 textCoord = scaledCoord + offset / 30;
        return composable.eval(textCoord / scale);
    }
"""

const val AGSL_KALEIDOSCOPE_EFFECT = """
    uniform shader composable;
    uniform float2 iSize;
    uniform float iTime;
    uniform float timeMultiplier;
    uniform float rippleMultiplier; // Used to modify iteration depth
    uniform float zoomMultiplier;
    uniform float dirMultiplier; // Used to modify the direction
    uniform int fractalType; // 0 for Mandelbrot, 1 for Julia
    uniform float scale; // Scale of the fractal

    const int MAX_ITERATIONS = 100; // Adjust as needed for performance and detail

    half4 main(float2 fragCoord) {
        // Scale and zoom the coordinates
        float scale = 1.0 / iSize.x;
        float2 scaledCoord = fragCoord * scale * zoomMultiplier;

        // Use rippleMultiplier to modify iteration depth
        int iterationDepth = int(rippleMultiplier * 0.5) + 1;

        // Calculate the fractal coordinates
        float2 c = (fragCoord - iSize * 0.5) / iSize.x * -float2(1.5, 1.5);
        float2 z = c * sin((c.x * c.y) * dirMultiplier + iTime) - cos(rippleMultiplier + iTime * (c.x * c.y));

        // Initialize the iteration counter
        int j = 0;

        // Iterate to compute the fractal
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            j = i;
            if (i >= iterationDepth || dot(z - dirMultiplier, z - dirMultiplier) > 3.0) break;
         
            z = float2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
        }

        // Calculate the intensity based on the number of iterations
        float intensity = float(j) / float(iterationDepth);

        // Calculate the offset for texture coordinates
        float2 offset = z * intensity + cos(iTime * timeMultiplier);

        // Adjust the texture coordinates with the offset
        float2 textCoord = scaledCoord + float2(cos(offset.x), sin(offset.y)) / 30.0;

        // Retrieve the base color using composable.eval
        half4 baseColor = composable.eval(textCoord * zoomMultiplier / scale);

        // Return the final color
        return baseColor;
    }
"""

