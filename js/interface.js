var CanvasInterface = function(options) {

    this._scene = null;
    this._mouseController = null;
    this._renderMode = 0;
    this._modelModifier = {};

    this._console = options.console ? $(options.console) : undefined;
    
    this._initializeGraphics = function() {
        var context = this;
        
        CubicVR.start('auto', function(gl, canvas){       
            context._scene = new CubicVR.Scene({
                
                camera: {
                    name: "the_camera",
                    fov: 60.0,
                    targeted: true,
                    position: [0, 125, 125],
                    rotation: [-15.0, 0.0, 180.0],
                    width: canvas.width,
                    height: canvas.height,
                    distance: 500
                },
                
                light: {
                    name: "the_light",
                    type: "point",
                    method: "dynamic",
                    diffuse: [1, 1, 1],
                    position: [0, 150, 150],
                    specular:[1,1,1],
                    distance: 300
                    
                }
            });

            context._modelModifier = {
                name: "terrainMesh",
                scale: [1.0, 1.0, 0.01],
                motion: new CubicVR.Motion({
                    rotation: {
                        0.0: {
                            z: 0
                        },
                        4.0: {
                            z: 90
                        },
                        envelope: {
                            behavior: "offset"
                        }
                    },
                    envelope: {
                        behavior: "repeat"
                    }
                })
            };
				
            context._mouseController = new CubicVR.MouseViewController(canvas, context._scene.camera);

            CubicVR.addResizeable(context._scene.getCamera());
        
            CubicVR.MainLoop(function(timer, gl) {
                context._scene.evaluate(timer.getSeconds());
                context._scene.render();
            });
        });
    }
    
    this._initializeInterface = function() {
        var context = this;
        context._log(context, "Press <strong>P</strong> to toggle request parameters.");
        context._log(context, "Press <strong>J</strong> to load JSON model.");
        context._log(context, "Press <strong>C</strong> to load COLLADA model.");
        context._log(context, "Press <strong>SPACE</strong> to cycle rendering modes.");
        
        $(document).keyup(function(e){
            if(e.which == 80) {
                $("#parameters").toggle();
            }
            else if(e.which == 74) {
                context.requestJsonModel("data/request.template", context._collateParameters({
                    mimeType: "application/json"
                }));
                context._log(context, "Loading JSON model...");
            }
            else if(e.which == 67) {
                context.requestColladaModel("data/request.template", context._collateParameters({
                    mimeType: "model/vnd.collada+xml"
                }));
                context._log(context, "Loading COLLADA model...");
            }
            else if(e.which == 32) {
                context._renderMode = (context._renderMode + 1) % 3;
                
                var mesh = context._scene.getSceneObject(context._modelModifier.name) ? context._scene.getSceneObject(context._modelModifier.name).obj : undefined;
                if(mesh) {
                    switch(context._renderMode) {
                        case 0:
                            mesh.setWireframe(false);
                            mesh.setPointMode(false);
                            context._log(context, "Rendering mode set to <strong>TEXTURED</strong>.");
                            break;
                        case 1:
                            mesh.setWireframe(true);
                            mesh.setPointMode(false);
                            context._log(context, "Rendering mode set to <strong>WIREFRAME</strong>.");
                            break;
                        case 2:
                            mesh.setWireframe(false);
                            mesh.setPointMode(true);
                            context._log(context, "Rendering mode set to <strong>POINT</strong>.");
                            break;
                    }
                }
            }
        });        
    }
    
    this._collateParameters = function(options) {
        var parameters = {
            url:        $("#paramUrl").val(),
            identifier: $("#paramWorkspace").val() + ":" + $("#paramLayer").val(),
            coverage:   $("#paramCoverage").val()
        };
        
        if(options) {
            $.extend(parameters, options);
        }
        
        return parameters;
    }
	
    this._log = function(context, message) {
        
        if(context._console) {
            context._console.append(message).append("<br/>");
            context._console.scrollTop(context._console[0].scrollHeight);
        }
        else {
            console.log(message);
        }
    }
    
    this._requestData = function(dataURL, callback) {
        $.ajax({
            url: dataURL,
            type: "GET",
            dataType: "text",
            success: function(data, textStatus, jqXHR ) {
                callback(data);
            },
            error: function(jqXHR, textStatus, errorThrown ) {
                context._log(context, errorThrown);
            }
        });
    }
    
    this._reformatTemplate = function(data, tokens) {
        $.each(tokens, function(key, value) {
            data = data.replace(new RegExp("{"+key+"}", "g"), value);
        });
        
        return data;
    }
    
    this.requestJsonModel = function(requestDataURL, parameters) {
        var context = this;
        
        this._requestData(requestDataURL, function(requestData) {
            requestData = context._reformatTemplate(requestData, parameters);
            
            context._log(context, "Fetching JSON model...");
            $.ajax({
                url: parameters.url,
                type: "POST",
                contentType: "application/xml",
                data: requestData,
                dataType: "json",
				
                success: function(modelData, textStatus, jqXHR ) {
                    context._log(context, "Generating scene object from model...");
                    if(context._scene.getSceneObject(context._modelModifier.name)) {
                        context._scene.removeSceneObject(context._scene.getSceneObject(context._modelModifier.name));
                    }
                    
                    var meshObject = new CubicVR.Mesh(modelData);
                    meshObject.compile();
                    var translate = (new CubicVR.Transform()).translate((meshObject.bb[1][0] - meshObject.bb[0][0]) / -2, (meshObject.bb[1][1] - meshObject.bb[0][1]) / -2, 0.0);
                    
                    var translatedMesh = new CubicVR.Mesh();
                    translatedMesh.booleanAdd(meshObject, translate);
                    translatedMesh.buildEdges();
                    translatedMesh.prepare();
                    
                    var meshSceneObject = new CubicVR.SceneObject({
                        mesh: translatedMesh
                    });
                    translatedMesh.prepare();
                    
                    $.each(context._modelModifier, function(key, value) {
                        meshSceneObject[key] = value;
                    });
                    
                    context._scene.bindSceneObject(meshSceneObject);
                    context._renderMode = 0;
                    context._log(context, "Loading complete.");
                },
				
                error: function(jqXHR, textStatus, errorThrown ) {
                    context._log(context, errorThrown);
                }
            });
        });
    }
	
    this.requestColladaModel = function(requestDataURL, parameters) {
        var context = this;
        
        this._requestData(requestDataURL, function(requestData) {
            requestData = context._reformatTemplate(requestData, parameters);

            context._log(context, "Fetching COLLADA model...");                
            $.ajax({
                url: parameters.url,
                type: "POST",
                contentType: "application/xml",
                data: requestData,
                dataType: "xml",

                success: function(modelData, textStatus, jqXHR ) {
                    context._log(context, "Generating scene object from model...");
                    if(context._scene.getSceneObject(context._modelModifier.name)) {
                        context._scene.removeSceneObject(context._scene.getSceneObject(context._modelModifier.name));
                    }
                    
                    var colladaScene = CubicVR.loadCollada(CubicVR.util.xml2badgerfish(modelData));
                    
                    $.each(colladaScene.sceneObjects, function(i, sceneObject) {
                        var translate = (new CubicVR.Transform()).translate((sceneObject.obj.bb[1][0] - sceneObject.obj.bb[0][0]) / -2, (sceneObject.obj.bb[1][1] - sceneObject.obj.bb[0][1]) / -2, 0.0);

                        var translatedMesh = new CubicVR.Mesh();
                        translatedMesh.booleanAdd(sceneObject.obj, translate);
                        translatedMesh.buildEdges();
                        translatedMesh.prepare();
                    
                        var meshSceneObject = new CubicVR.SceneObject(translatedMesh);
					
                        $.each(context._modelModifier, function(key, value) {
                            meshSceneObject[key] = value;
                        });
				
                        context._scene.bindSceneObject(meshSceneObject);
                    });
                    
                    context._renderMode = 0;
                    context._log(context, "Loading complete.");
                },
				
                error: function(jqXHR, textStatus, errorThrown ) {
                    context._log(context, errorThrown);
                }
            });
        });
    }
	    
    {
        this._initializeGraphics();
        this._initializeInterface();
    }
}
