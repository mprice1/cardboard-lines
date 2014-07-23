#version 300 es

uniform mat4 u_P;

in mat4 a_MV;
in vec4 a_Position;
in vec2 a_Texcoord;
in vec3 a_Normal;

out vec2 v_Texcoord;
out vec3 v_Position;
out vec3 v_Normal;

void main() {
  v_Texcoord = a_Texcoord;
  v_Normal = a_Normal;
  v_Position = vec3(u_MV * a_Position);
  gl_Position = u_P * u_MV * a_Position;
}
