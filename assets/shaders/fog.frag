// Ported from flame's crystal_ball example fog.frag (bundled in the Dart
// source at shaders/fog.frag). Same porting note as shockwave.frag:
// FlutterFragCoord()/uSize -> v_texCoords.
#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;

uniform vec2 uSize;
uniform float uGroundPos;
uniform float uGroundAdd;
uniform float uFade;
uniform float uTime;

float hash(vec2 p) {
    p = 50.0 * fract(p * 0.3183099 + vec2(0.71, 0.113));
    return -1.0 + 2.0 * fract(p.x * p.y * (p.x + p.y));
}

float noise(in vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    return mix(mix(hash(i + vec2(0.0, 0.0)), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fractalNoise(vec2 uv) {
    float f = 0.0;
    uv *= 8.0;
    mat2 m = mat2(1.6, 1.2, -1.2, 1.6);
    f = 0.5000 * noise(uv);
    uv = m * uv;
    f += 0.2500 * noise(uv);
    uv = m * uv;
    f += 0.1250 * noise(uv);
    uv = m * uv;
    f += 0.0625 * noise(uv);
    return 0.5 + 0.5 * f;
}

vec4 layer(vec2 uv, float timeMultiplier) {
    float waterline = uGroundPos;
    float fade = uFade;

    float tr = step(waterline - fade, uv.y);
    tr *= smoothstep(waterline - fade, waterline, uv.y);
    uv.y -= uGroundAdd;
    uv.x += uTime * timeMultiplier;

    float f = fractalNoise(uv);
    f *= tr;
    f *= 0.65;
    f = pow(f, 1.8);
    return vec4(vec3(0.8, 0.4, 1.0) * f, f);
}

void main() {
    vec2 uv = v_texCoords * vec2(uSize.x / uSize.y, 1.0);
    gl_FragColor = layer(uv, 0.015) + layer(uv, -0.08);
}
