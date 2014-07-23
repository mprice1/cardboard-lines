#version 300 es

uniform mat4 u_MVP;
uniform mat4 u_MV;
uniform mat4 u_M;

in vec4 a_Position;
in vec2 a_Texcoord;
in vec3 a_Normal;

out vec2 v_Texcoord;
out vec3 v_Position;
out vec3 v_Normal;

void main() {
  v_Texcoord = a_Texcoord;
  v_Normal = a_Normal;
  vec4 position = a_Position;
  position.x = position.x + float(gl_InstanceID);
  v_Position = vec3(u_MV * position);
  gl_Position = u_MVP * position;
}
