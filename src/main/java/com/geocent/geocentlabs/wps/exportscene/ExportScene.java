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

import java.awt.image.DataBufferShort;
import java.awt.image.RenderedImage;
import java.io.IOException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.ProgressListener;

/**
 * This class is the main interface to the elevation scene exporter and implements
 * the GSProcess class to provide WPS functionality.
 * 
 * Current implementation supports exporting the terrain mesh to an industry
 * standard <a href = "http://www.khronos.org/collada/">COLLADA v1.4.1</a> asset file.
 * A reference implementation is also included which allows for exporting the
 * terrain mesh to a JSON format supported by the
 * <a href = "https://github.com/cjcliffe/CubicVR.js">CubicVR.js</a> javascript
 * framework.
 * 
 * @author Joshua Penton
 */
@DescribeProcess(title = "exportScene", description = "WPS 3-D Scene Model Explorer")
public class ExportScene implements GSProcess {
    
    @DescribeResult(name = "scene", description = "Exported Scene")
    public SceneType execute(
            @DescribeParameter(name = "coverage", description = "Name of raster") GridCoverage2D sceneCoverage,
            ProgressListener progressListener)
            throws IOException, TransformException {
        
        RenderedImage sceneImage = sceneCoverage.getRenderedImage();
        
        short[] sceneData = ((DataBufferShort) sceneImage.getData().getDataBuffer()).getData();
        
        SceneType sceneType = new SceneType();
        int offset = 0;
        
        for(int y=0; y < sceneImage.getHeight(); y++) {
            for(int x=0; x < sceneImage.getWidth(); x++) {
                sceneType.insertCoordinate(x, y, sceneData[offset++]);
            }
        }
        
        sceneType.setSceneSize(sceneImage.getWidth(), sceneImage.getHeight());
        sceneType.setSceneEnvelope(sceneCoverage.getEnvelope2D());
        
        return sceneType;
    }    
}
