#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_windTime;
uniform float u_windStrength;

void main() {
    // Sway gradient: zero at tile base (uv.y = 0), maximum at tile tip (uv.y = 1).
    // LibGDX SpriteBatch uses bottom-left UV origin, so uv.y=1 is the top of the sprite.
    float gradient = v_texCoords.y;

    // Horizontal UV displacement: blades at different x positions sway slightly out of phase
    // (x * 6.0 term), creating a natural ripple rather than a rigid wall movement.
    // 0.03 = max offset (~2px on a 64px-wide tile) at full wind strength at the tip.
    float sway = sin(u_windTime * 2.5 + v_texCoords.x * 6.0)
                 * u_windStrength * gradient * 0.03;

    vec2 uv = vec2(clamp(v_texCoords.x + sway, 0.0, 1.0), v_texCoords.y);

    gl_FragColor = v_color * texture2D(u_texture, uv);
}
