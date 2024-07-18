package com.example.myapplication.data

sealed interface ShaderAttribute {
    data class FloatUniform(val float: Float) : ShaderAttribute
    data class Float2VecUniform(val float2Vec: Vector2) : ShaderAttribute
    data class Float3VecUniform(val float3Vec: Vector3) : ShaderAttribute
}

data class Vector3(
    val x: Float = 0.0f,
    val y: Float = 0.0f,
    val z: Float = 0.0f,
)

data class Vector2(
    val x: Float = 0.0f,
    val y: Float = 0.0f,
)