package com.ldm.basic.filter;

/**
 * Created by wangyang on 15/7/23.
 * FrameRenderer
 */
public class FrameRendererEdge extends FrameRendererEmboss {

        private static final String vshEdge = "" +
                "attribute vec2 vPosition;\n" +
                "varying vec2 texCoord;\n" +
                "varying vec2 coords[8];\n" +

                "uniform vec2 samplerSteps;\n" +
                "const float stride = 2.0;\n" +
                "uniform mat2 rotation;\n" +

                "void main()\n" +
                "{\n" +
                "  gl_Position = vec4(vPosition, 0.0, 1.0);\n" +
                "  texCoord = (vec2(vPosition.x, -vPosition.y) / 2.0) * rotation + 0.5;\n" +

                "  coords[0] = texCoord - samplerSteps * stride;\n" +
                "  coords[1] = texCoord + vec2(0.0, -samplerSteps.y) * stride;\n" +
                "  coords[2] = texCoord + vec2(samplerSteps.x, -samplerSteps.y) * stride;\n" +

                "  coords[3] = texCoord - vec2(samplerSteps.x, 0.0) * stride;\n" +
                "  coords[4] = texCoord + vec2(samplerSteps.x, 0.0) * stride;\n" +

                "  coords[5] = texCoord + vec2(-samplerSteps.x, samplerSteps.y) * stride;\n" +
                "  coords[6] = texCoord + vec2(0.0, samplerSteps.y) * stride;\n" +
                "  coords[7] = texCoord + vec2(samplerSteps.x, samplerSteps.y) * stride;\n" +

                "}";

        private static final String fshEdge = "" +
                "precision mediump float;\n" +
                "varying vec2 texCoord;\n" +
                "uniform %s inputImageTexture;\n" +
                "varying vec2 coords[8];\n" +

                "void main()\n" +
                "{\n" +
                "  vec3 colors[8];\n" +

                "  for(int i = 0; i < 8; ++i)\n" +
                "  {\n" +
                "    colors[i] = texture2D(inputImageTexture, coords[i]).rgb;\n" +
                "  }\n" +

                "  vec4 src = texture2D(inputImageTexture, texCoord);\n" +

                "  vec3 h = -colors[0] - 2.0 * colors[1] - colors[2] + colors[5] + 2.0 * colors[6] + colors[7];\n" +
                "  vec3 v = -colors[0] + colors[2] - 2.0 * colors[3] + 2.0 * colors[4] - colors[5] + colors[7];\n" +

                "  gl_FragColor = vec4(sqrt(h * h + v * v), 1.0);\n" +
                "}";


        public static FrameRendererEdge create(boolean isExternalOES) {
                FrameRendererEdge renderer = new FrameRendererEdge();
                if(!renderer.init(isExternalOES)) {
                        renderer.release();
                        return null;
                }
                return renderer;
        }

        @Override
        public String getFragmentShaderString() {
                return fshEdge;
        }

        @Override
        public String getVertexShaderString() {
            return vshEdge;
    }

}
