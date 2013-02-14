Geoserver 3-D Model Scene Exporter
==================================

This WPS plugin for Geoserver provides a service to export raster elevation data
from a coverage store to a 3-D model for integration with external tools or
frameworks.

The current implementation of the project supports exporting the terrain mesh to
an industry standard [COLLADA](http://www.khronos.org/collada/) v1.4.1 asset
file. A reference implementation is also included which allows for exporting the
terrain mesh to a JSON format supported by the
[CubicVR.js](https://github.com/cjcliffe/CubicVR.js) javascript
framework.

Building the Project
--------------------
The project's is developed utilizing the [Maven](http://maven.apache.org/) build
system. To build the projects either import the project into your editor of
choice which supports Maven projects (such as [Netbeans](http://netbeans.org/)
or [Eclipse](http://www.eclipse.org/)) and build the project using the IDE's
build tools.

Alternately from the project's root directory run the following command:
`mvn clean install`

Deploying the Project
---------------------
To deploy the project first build the project following the directions above
then copy the built artifact to your `[GEOSERVER HOME]/WEB-INF/lib` directory.

Alternately the project includes a Maven profile `deploy-geoserver` which will
locally deploy the built artifact to the directory specified my the Maven
property `geoserver.home.dir`. To set this property either override the value in
your `settings.xml` file or modify the default value in the project's `pom.xml`
file to match the configuration of your system.

To activate the profile either activate the profile in your IDE build settings
or from the project's root directory run the following command:
`mvn clean install -Pdeploy-geoserver`.

Example WPS Request
-------------------
An example WPS request which utilizes WPS chaining to extract data from the
`workspace:layer` layer utilizing the inbuilt `gs:CropCoverage` WPS and convert
the data to a COLLADA model is included below.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<wps:Execute version="1.0.0" service="WPS" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.opengis.net/wps/1.0.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:wcs="http://www.opengis.net/wcs/1.1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd">
  <ows:Identifier>gs:ExportScene</ows:Identifier>
  <wps:DataInputs>
    <wps:Input>
      <ows:Identifier>coverage</ows:Identifier>
      <wps:Reference mimeType="image/tiff" xlink:href="http://geoserver/wps" method="POST">
        <wps:Body>
          <wps:Execute version="1.0.0" service="WPS">
            <ows:Identifier>gs:CropCoverage</ows:Identifier>
            <wps:DataInputs>
              <wps:Input>
                <ows:Identifier>coverage</ows:Identifier>
                <wps:Reference mimeType="image/tiff" xlink:href="http://geoserver/wcs" method="POST">
                  <wps:Body>
                    <wcs:GetCoverage service="WCS" version="1.1.1">
                      <ows:Identifier>workspace:layer</ows:Identifier>
                      <wcs:DomainSubset>
                        <gml:BoundingBox crs="http://www.opengis.net/gml/srs/epsg.xml#4326">
                          <ows:LowerCorner>-180.0125 -55.00416666666665</ows:LowerCorner>
                          <ows:UpperCorner>180.0125 84.00416666666668</ows:UpperCorner>
                        </gml:BoundingBox>
                      </wcs:DomainSubset>
                      <wcs:Output format="image/tiff"/>
                    </wcs:GetCoverage>
                  </wps:Body>
                </wps:Reference>
              </wps:Input>
              <wps:Input>
                <ows:Identifier>cropShape</ows:Identifier>
                <wps:Data>
                  <wps:ComplexData mimeType="text/xml; subtype=gml/3.1.1"><![CDATA[POLYGON ((-156.10 18.90, -154.75 18.90, -154.75 20.35, -156.10  20.35, -156.10 18.90))]]></wps:ComplexData>
                </wps:Data>
              </wps:Input>
            </wps:DataInputs>
            <wps:ResponseForm>
              <wps:RawDataOutput mimeType="image/tiff">
                <ows:Identifier>result</ows:Identifier>
              </wps:RawDataOutput>
            </wps:ResponseForm>
          </wps:Execute>
        </wps:Body>
      </wps:Reference>
    </wps:Input>
  </wps:DataInputs>
  <wps:ResponseForm>
    <wps:RawDataOutput mimeType="model/vnd.collada+xml">
      <ows:Identifier>scene</ows:Identifier>
    </wps:RawDataOutput>
  </wps:ResponseForm>
</wps:Execute>
```

To modify this request to export the scene to the CubicVR.js JSON schema simply
change the `mimeType` attribute of the `wps:RawDataOutput` tag to
`application/json`.

License
-------
Permissive free open source [MIT](http://opensource.org/licenses/MIT/) software
license.

Copyright (C) 2013 [Geocent](http://geocent.com/)

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.