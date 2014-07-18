precision highp float;

uniform sampler2D u_Texture;

uniform vec2 u_TexScale;
uniform vec3 u_BgColor;

varying vec2 v_Texcoord;
varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
  vec4 texColor = texture2D(u_Texture, v_Texcoord * u_TexScale);
  gl_FragColor.rgb = mix(u_BgColor, texColor.rgb, texColor.a);
  gl_FragColor.a = 1.0;
}