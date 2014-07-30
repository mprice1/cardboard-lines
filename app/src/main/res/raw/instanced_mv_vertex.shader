#version 300 es

uniform mat4 u_P;
uniform mat4 u_MV;

// in vec4 a_MV0;
// in vec4 a_MV1;
// in vec4 a_MV2;
// in vec4 a_MV3;

in vec4 a_Position;
in vec2 a_Texcoord;
in vec3 a_Normal;
in float a_ID;

out vec2 v_Texcoord;
out vec3 v_Position;
out vec3 v_Normal;

void main() {
  v_Texcoord = a_Texcoord;
  v_Normal = a_Normal;
  v_Position = vec3(u_MV * a_Position);
  gl_Position = u_P * u_MV * (a_Position + vec4(a_ID * 2.0,0.0,0.0,0.0));
}
