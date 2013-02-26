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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.collada._2005._11.colladaschema.COLLADA.Scene;
import org.collada._2005._11.colladaschema.*;
import org.geoserver.wps.ppio.XMLPPIO;
import org.xml.sax.ContentHandler;

/**
 * This class binds the conversion of the computed terrain mesh to the
 * "model/vnd.collada+xml" mimeType attribute of the wps:RawDataOutput for the
 * implemented WPS. The output is a very minimal
 * <a href = "http://www.khronos.org/collada/">COLLADA</a> asset file conforming
 * to the 1.4.1 version of the specification.
 * 
 * @author jpenton
 */
public class ColladaScenePPIO extends XMLPPIO {

    private final static String IDENTIFIER_ROOT = "gis";
    private final static String IDENTIFIER_VISUAL_SCENE = IDENTIFIER_ROOT + "_visual_scene";
    private final static String IDENTIFIER_GEOMETRY = IDENTIFIER_ROOT + "_geometry";
    private final static String IDENTIFIER_MESH_SOURCE_VERTEX = IDENTIFIER_ROOT + "_mesh_source_vertex";
    private final static String IDENTIFIER_MESH_SOURCE_VERTEX_VALUES = IDENTIFIER_MESH_SOURCE_VERTEX + "_values";
    private final static String IDENTIFIER_MESH_VERTICES = IDENTIFIER_ROOT + "_mesh_vertices";

    private final Marshaller marshaller;
    
    protected ColladaScenePPIO() throws JAXBException {
        super(SceneType.class, SceneType.class, "model/vnd.collada+xml", new QName("http://www.collada.org/2005/11/COLLADASchema", "COLLADA"));
        
        JAXBContext jc = JAXBContext.newInstance( COLLADA.class );
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    }

    @Override
    public void encode(Object sceneObject, ContentHandler ch) throws Exception {
        marshaller.marshal(generateCollodaObject((SceneType)sceneObject), ch);
    }
    
    public static COLLADA generateCollodaObject(SceneType sceneObject) {
        COLLADA colladaObject = new COLLADA();
        colladaObject.setVersion("1.4.1");

        // Scene <scene>        
        Scene scene = new Scene();
        colladaObject.setScene(scene);
        
        InstanceWithExtra instanceVisualScene = new InstanceWithExtra();
        instanceVisualScene.setUrl("#" + IDENTIFIER_VISUAL_SCENE);
        scene.setInstanceVisualScene(instanceVisualScene);
        
        // Scene <library_visual_scenes>
        LibraryVisualScenes libraryVisualScenes = new LibraryVisualScenes();
        colladaObject.getLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras().add(libraryVisualScenes);
        
        VisualScene visualScene = new VisualScene();
        visualScene.setId(IDENTIFIER_VISUAL_SCENE);
        visualScene.setName(IDENTIFIER_VISUAL_SCENE);
        libraryVisualScenes.getVisualScene().add(visualScene);
        
        Node sceneNode = new Node();
        visualScene.getNode().add(sceneNode);
        
        InstanceGeometry instanceGeometry = new InstanceGeometry();
        instanceGeometry.setUrl("#"+IDENTIFIER_GEOMETRY);
        sceneNode.getInstanceGeometry().add(instanceGeometry);
        
        // Scene <library_geometries>
        LibraryGeometries libraryGeometry = new LibraryGeometries();
        colladaObject.getLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras().add(libraryGeometry);
        
        Geometry geometry = new Geometry();
        geometry.setId(IDENTIFIER_GEOMETRY);
        geometry.setName(IDENTIFIER_GEOMETRY);
        libraryGeometry.getGeometry().add(geometry);

        Mesh mesh = new Mesh();
        geometry.setMesh(mesh);

        // Mesh <source>
        Source sourceVertices = new Source();
        sourceVertices.setId(IDENTIFIER_MESH_SOURCE_VERTEX);
        sourceVertices.setName(IDENTIFIER_MESH_SOURCE_VERTEX);
        mesh.getSource().add(sourceVertices);

        FloatArray vertexArray = new FloatArray();
        vertexArray.setId(IDENTIFIER_MESH_SOURCE_VERTEX_VALUES);
        vertexArray.setName(IDENTIFIER_MESH_SOURCE_VERTEX_VALUES);
        sourceVertices.setFloatArray(vertexArray);

        Source.TechniqueCommon technique = new Source.TechniqueCommon();
        sourceVertices.setTechniqueCommon(technique);

        Accessor vertexAccessor = new Accessor();
        vertexAccessor.setSource("#" + IDENTIFIER_MESH_SOURCE_VERTEX_VALUES);
        vertexAccessor.setStride(BigInteger.valueOf(3));
        technique.setAccessor(vertexAccessor);

        Param paramX = new Param();
        paramX.setName("X");
        paramX.setType("float");
        vertexAccessor.getParam().add(paramX);

        Param paramY = new Param();
        paramY.setName("Y");
        paramY.setType("float");
        vertexAccessor.getParam().add(paramY);

        Param paramZ = new Param();
        paramZ.setName("Z");
        paramZ.setType("float");
        vertexAccessor.getParam().add(paramZ);

        // Mesh <vertices>
        Vertices vertices = new Vertices();
        vertices.setId(IDENTIFIER_MESH_VERTICES);
        vertices.setName(IDENTIFIER_MESH_VERTICES);
        mesh.setVertices(vertices);

        InputLocal inputLocal = new InputLocal();
        inputLocal.setSemantic("POSITION");
        inputLocal.setSource("#" + IDENTIFIER_MESH_SOURCE_VERTEX);
        vertices.getInput().add(inputLocal);

        // Mesh <triangles>
        Triangles triangles = new Triangles();
        mesh.getLinesOrLinestripsOrPolygons().add(triangles);

        InputLocalOffset inputLocalOffset = new InputLocalOffset();
        inputLocalOffset.setSemantic("VERTEX");
        inputLocalOffset.setSource("#" + IDENTIFIER_MESH_VERTICES);
        inputLocalOffset.setOffset(BigInteger.ZERO);
        triangles.getInput().add(inputLocalOffset);

        // Process the mesh faces
        HashMap<Vertex, Integer> facesVertices = new LinkedHashMap<Vertex, Integer>();
        List<Vertex[]> faceVertices = sceneObject.getFaces();
        for (Vertex[] face : faceVertices) {
            for (Vertex vertex : face) {
                if (!facesVertices.containsKey(vertex)) {
                    vertexArray.getValue().add(vertex.getX());
                    vertexArray.getValue().add(vertex.getY());
                    vertexArray.getValue().add(vertex.getZ());

                    facesVertices.put(vertex, facesVertices.size());
                }
                triangles.getP().add(BigInteger.valueOf(facesVertices.get(vertex)));
            }
        }
        
        triangles.setCount(BigInteger.valueOf(faceVertices.size()));
        vertexAccessor.setCount(BigInteger.valueOf(facesVertices.size()));
        vertexArray.setCount(BigInteger.valueOf(vertexArray.getValue().size()));
        
        return colladaObject;
    }

    @Override
    public String getFileExtension() {
        return "dae";
    }
    
    @Override
    public Object decode(InputStream in) throws Exception {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }
}
