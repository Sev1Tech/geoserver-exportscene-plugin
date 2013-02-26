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

package com.geocent.geocentlabs.wps.exportscene;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;
import java.util.LinkedList;
import java.util.List;
import org.geotools.geometry.Envelope2D;

/**
 * This object class serves as the interface object between the WPS interface class
 * and supporting PPIO classes. This class is also responsible for leveraging the JTS
 * <a href = "http://en.wikipedia.org/wiki/Delaunay_triangulation">Delaunay triangulation</a>
 * to create the underlying terrain mesh data structure to enable the supporting
 * PPIO classes to generate the final 3-D model.
 * 
 * @author Joshua Penton
 */
public class SceneType {
    private final List<Coordinate> pointCloud;
    private Envelope2D sceneEnvelope;
    
    private double sceneSizeX;
    private double sceneSizeY;
    
    public SceneType() {
        pointCloud = new LinkedList<Coordinate>();
    }

    public Envelope2D getSceneEnvelope() {
        return sceneEnvelope;
    }

    public void setSceneEnvelope(Envelope2D sceneEnvelope) {
        this.sceneEnvelope = sceneEnvelope;
    }

    public double getSceneSizeX() {
        return sceneSizeX;
    }

    public double getSceneSizeY() {
        return sceneSizeY;
    }
    
    public void setSceneSize(double x, double y) {
        this.sceneSizeX = x;
        this.sceneSizeY = y;
    }
    
    /**
     * Insert the three coordinate values into the point cloud for later triangulation.
     * 
     * @param x x parameter of the data point
     * @param y y parameter of the data point
     * @param z z parameter of the data point
     */
    public void insertCoordinate(float x, float y, float z) {
        pointCloud.add(new Coordinate(x, y, z));
    }
    
    /**
     * Performs a JTS Delaunay triangulation on the current state of the point
     * cloud and returns a list of faces comprising the resulting mesh.
     * 
     * @return a list of Vertex arrays corresponding to the faces of the mesh
     */
    public List<Vertex[]> getFaces() {
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(pointCloud);
        return builder.getSubdivision().getTriangleVertices(false);
    }
}
