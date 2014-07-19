precision highp float;

uniform sampler2D u_Texture;

uniform vec2 u_TexScale;
uniform vec3 u_BgColor;

varying vec2 v_Texcoord;
varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
  vec4 texColor = texture2D(u_Texture, v_Texcoord * u_TexScale);
  vec3 fogColor = vec3(1.0, 1.0, 1.0);
  float fogFactor = max(0.0, min(1.0, abs(v_Position.z / 50.0)));
  vec3 color = mix(u_BgColor, texColor.rgb, texColor.a);
  color = mix(color, fogColor, fogFactor);
  gl_FragColor.rgb = color.rgb;
  gl_FragColor.a = 1.0;
}