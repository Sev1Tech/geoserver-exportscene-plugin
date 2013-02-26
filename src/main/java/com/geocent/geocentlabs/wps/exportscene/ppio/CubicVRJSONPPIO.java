/*
 * Copyright (c) 2013 Geocent, LLC
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.geocent.geocentlabs.wps.exportscene.ppio;

import com.geocent.geocentlabs.wps.exportscene.SceneType;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.geoserver.wps.ppio.CDataPPIO;

/**
 * This class binds the conversion of the computed terrain mesh to the
 * "application/json" mimeType attribute of the wps:RawDataOutput for the
 * implemented WPS. The output is a JSON representation of the terrain mesh which
 * conforms to the specification of the
 * <a href = "https://github.com/cjcliffe/CubicVR.js">CubicVR.js</a> javascript
 * framework.
 * 
 * @author jpenton
 */
public class CubicVRJSONPPIO extends CDataPPIO {
    protected CubicVRJSONPPIO() {
        super(SceneType.class, SceneType.class, "application/json");
    }
    
    @Override
    public void encode(Object sceneObject, OutputStream out) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", "terrainMesh");

        // Points/Faces Section
        JSONArray jsonPoints = new JSONArray();
        JSONArray jsonFaces = new JSONArray();
        
        HashMap<Vertex, Integer> vertices = new LinkedHashMap<Vertex, Integer>();
        for(Vertex[] face : ((SceneType)sceneObject).getFaces()) {
            JSONArray jsonFace = new JSONArray();
            for(Vertex vertex : face) {
                if(!vertices.containsKey(vertex)) {
                    JSONArray jsonPoint = new JSONArray();
                    jsonPoint.add(vertex.getX());
                    jsonPoint.add(vertex.getY());
                    jsonPoint.add(vertex.getZ());                
                    jsonPoints.add(jsonPoint);
                    
                    vertices.put(vertex, vertices.size());
                }
                jsonFace.add(vertices.get(vertex));

            }
            jsonFaces.add(jsonFace);
        }
        
        // Build the Object
        jsonObject.put("points", jsonPoints);
        jsonObject.put("faces", jsonFaces);

        out.write(jsonObject.toString().getBytes());
    }

    @Override
    public String getFileExtension() {
        return "json";
    }

    @Override
    public Object decode(String string) throws Exception {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    @Override
    public Object decode(InputStream in) throws Exception {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }
}
