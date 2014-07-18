uniform mat4 u_MVP;
uniform mat4 u_MV;
uniform mat4 u_M;

attribute vec4 a_Position;
attribute vec2 a_Texcoord;
attribute vec3 a_Normal;

varying vec2 v_Texcoord;
varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
  v_Texcoord = a_Texcoord;
  v_Normal = a_Normal;
  v_Position = vec3(u_M * a_Position);
  gl_Position = u_MVP * a_Position;
}
