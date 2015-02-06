precision mediump float;

uniform sampler2D u_TextureUnit;
uniform float u_Saturation;
varying vec2 v_TextureCoordinates;


//while looking to implement my rgb2hsv filter, I spotted this improved implementation. 
//Thus it's not mine. My implementation is commented below
vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

/*vec3 RGB2HSV(vec3 c){
    float rgb_max = max(c.r, max(c.g, c.b));
    float rgb_min = min(c.r, min(c.g, c.b));
    float delta = rgb_max - rgb_min;
    vec3 result;
    result.y = delta / (rgb_max + 1.0e-20);
    result.z = rgb_max;

    float hue;
    if (c.r == rgb_max)
        hue = (c.g - c.b) / (delta + 1.0e-20);
    else if (c.g == rgb_max)
        hue = 2.0 + (c.b - c.r) / (delta + 1e-20);
    else
        hue = 4.0 + (c.r - c.g) / (delta + 1e-20);
    if (hue < 0.0)
        hue += 6.0;
    result.x = hue * (1.0 / 6.0);
    return result;
}*/

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main()
{
	vec3 tmp = texture2D(u_TextureUnit, v_TextureCoordinates).rgb;
	vec3 c = rgb2hsv(tmp);
	c.y = u_Saturation*c.y;
	vec3 b = hsv2rgb(c);
    gl_FragColor = vec4(b.r, b.g, b.b, 1);
}
