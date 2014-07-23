#version 300 es
precision highp float;

uniform sampler2D u_Texture;

uniform vec2 u_TexScale;

out vec4 fragmentColor;

in vec2 v_Texcoord;
in vec3 v_Position;
in vec3 v_Normal;

void main() {
  vec4 texColor = texture(u_Texture, v_Texcoord * u_TexScale);
  vec3 fogColor = vec3(1.0, 1.0, 1.0);
  float fogFactor = max(0.0, min(1.0, abs(v_Position.z / 50.0)));
  vec3 color = texColor.rgb;
  color = mix(color, fogColor, fogFactor);
  fragmentColor.rgb = color.rgb;
  fragmentColor.a = 1.0;
}