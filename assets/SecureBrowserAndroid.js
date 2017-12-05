/* 
 Educational Online Test Delivery System
 Copyright (c) 2015 American Institutes for Research

 Distributed under the AIR Open Source License, Version 1.0
 See accompanying file AIR-License-1_0.txt or at
 http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*/

var AIRMobile = {
		
    /** (Internal)Command to request the device initialize.
     *  @constant */
	CMD_INITIALIZE: "cmdInitialize",
    /** (Internal)Command to request that the device speak a string of text
     @constant */
    CMD_SPEAK_TEXT: "cmdSpeakText",
    /** (Internal)Command to request that the device stop speaking text
     @constant */
    CMD_STOP_SPEAKING: "cmdStopSpeaking",
    /** (Internal)Command to request that the device pause speaking (stt)
     @constant */
    CMD_PAUSE_SPEAKING: "cmdPauseSpeaking",
    /** (Internal)Command to request that the device resume speaking (stt)
     @constant */
    CMD_RESUME_SPEAKING: "cmdResumeSpeaking",
    /** (Internal)Command to set the default url to load in the application.
    @constant */
    CMD_SET_DEFAULT_URL: "cmdSetDefaultURL",
    /** (Internal)Command to restore the default url to the original value.
    @constant */
    CMD_RESTORE_DEFAULT_URL: "cmdRestoreDefaultURL",
    /** (Internal)Command to request the device check the status of TTS.
     *  @constant */
    CMD_CHECK_TTS: "cmdCheckTTS",
    /** (Internal)Command to request that the device exit the application. Currently android only.
    @constant */
    CMD_EXIT_APPLICATION: "cmdExitApplication",
    /** (Internal)Command to request that the device restart the application. Currently android only.
    @constant */
    CMD_RESTART_APPLICATION: "cmdRestartApplication",

    ttsCallbackId: null,
    recorderCallbackId: null,
    audioCallbackId: null,
    version: 3,
    eventsCallbackId: new Map(),

    /**
     *  @ignore @private Registered callbacks.
     */
    callbacks: {},

    /** @namespace Represents the current status of the device. Users should treat
     *  the properties of this object as read only. Additionally, some properties may
     *  only apply to one type of software, iPhone OS devices, and Android devices, when
     *  this occurs, it is indicated in the properties documentation. */
    device: {

	    /** Status of the device. When true, the device has responded to
	     *  the initialization request and the fields have been set with
	     *  values reflecting it's state.
	     *  @type Boolean
	     */
	    isReady: false,

	    /** The AIRMobile version that the native application was built against.
	     *  A negative value indicates the field is uninitialized or unknown.
	     *  @type Number
	     */
	    apiVersion: -1.0,

	    /** The intial url that the application loads when started.
	     *  @type String
	     */
	    defaultURL: "Unknown",

	    /** Jailbroken/Rooted Status. When true, the native application has
	     *  detected that the device has been jailbroken or rooted. Note that
	     *  the application may not be able to detect this under all circumstances.
	     *  For instance, on iOS the device may check for the presence of a directory
	     *  typically found on jailbroken devices. The absence of this directory
	     *  does not mean the device is not jailbroken, only that we have not detected
	     *  that it is jailbroken.
	     *  @type boolean
	     */
	    rootAccess: false,

	    /** The device model. A String representing the model of the hardware.
	     *  eg. iPad, iPhone
	     *  @type String
	     */
	    model: "Unknown",

	    /** The device manufacturer. A string representing the device manufacturer.
	     *	eg. Apple, Samsung etc.
	     *	@type String
	     */
	    manufacturer: "Unknown",

	    /** The name of the operating system running on the device.
	     *  eg. iphone OS
	     *  @type String
	     */
	    operatingSystem: "Unknown",

	    /** The version of the operating system running on the device.
	     *  @type String
	     */
	    operatingSystemVersion: "Unknown",

	    /** The internet connectivity status of the device. <br>
	     *  Possible values include: <ul>
	     *  <li>'connected'</li>
	     *  <li>'disconnected'</li>
	     *  </ul>
	     *  @type String
	     */
	    connectivityStatus: "Unknown",

	    /** Status of Guided Access (iPhone OS ONLY). When true, guided access is
	     *  enabled on the device. Otherwise, guided access is disabled.
	     *  *Note: On android devices, this property will always be false, as android
	     *  devices do not have a guided access mode.
	     *  @type Boolean
	     */
	    guidedAccessEnabled: false,

	    /** Status of Text-To-Speech on the device. *Note this refers to the state native accessibility framework for the device.
	     *  On iOS, this corresponds to the "VoiceOver" accessibility option. On android this corresponds to the TalkBack accessibility
	     *  setting.
	     *  @type Boolean
	     */
	    textToSpeechEnabled: false,

	    /** Status of Text-To-Speech engine on the device. To receive notifications on status change
	     *  use EVENT_TTS_CHANGED. This property refers to the capability the application has of speaking
	     *  text passed via javascript. If the device is capable of handling these requests, its state will
	     *  be either 'idle' or 'playing', otherwise, a status of 'unavailable' will be reported.
	     *	Possible values include: <ul>
	     *	<li>'idle' - A TTS engine is available for use, and is not currently playing audio</li>
	     *	<li>'playing' - A TTS engine is available for use, and is currently playing audio</li>
	     *  <li>'unavailable' - A TTS engine is not available on the device. </li>
	     *	@type String
	     */
	    ttsEngineStatus: "unavailable",

	    /** A list of running processes on the device.
	     *  @type Array
	     */
	    runningProcesses: [],

	    /** A list of installed packages on the device (Android ONLY)
	     * @type Array
	     */
	    installedPackages: [],

	    /** The devices screen resolution.
	     * eg. "{width, height}"
	     */
	    screenResolution: "Unknown",

	    /** The devices current orientation.
	     *	Possible values include: <ul>
	     *  <li>'portrait'</li>
	     *  <li>'landscape'</li>
	     *	<li>'unknown'</li>
	     *  </ul>
	     *	@type String
	     */
	    orientation: "Unknown",

	    /** The status of orientation lock.
	     *	Possible values include: <ul>
	     *	<li>'portrait'</li>
	     *	<li>'landscape'</li>
	     *	<li>'none'</li>
	     *	</ul>
	     *	@type String
	     */
	    lockedOrientation: "none",

	    /** The keyboard that users device is configured to use.
	     *  This property is currently unique to android as the user can swap out
	     *  the keyboard. The included keyboard, which the user is required to switch
	     *  to currently has the package name: 'com.smarterapp.mobilebrowser/.softkeyboard.SoftKeyboard'
	     *  @type String
	     */
	    keyboard: "unknown",

	    /** The status of the devices microphone.
         @type Boolean */
	    micMuted: false,

	    /** The IP address of the device.
         @type String */
        ipAddress : "unknown",

        /** The MAC address of the device.
         @type String */
        macAddress : "unknown",

        /** A list of voice packs supported by the browser.
         @type String */
        ttsVoices: "unknown",

        /** tts pitch.
         @type integer */
        ttsPitch: 0,

        /** tts rate.
         @type integer */
        ttsRate: 0,

        /** tts volume.
         @type integer */
        ttsVolume: 0,


	    /** Check if the device can currently take screenshots. For iphone OS,
	     *  this returns true when guided access is disabled, false otherwise. On
	     *  android, this function always returns false, this is because the native application
	     *  blocks the standard mechanism for taking screenshots. Users should also inspect
	     *  the installed applications property for android devices to ensure that the user
	     *  does not have an application that directly accesses the screenbuffer installed.
	     *  @return {Boolean} true if it has been detected that the device can currently take a sceenshot by standard means.
	     */
	    screenShotsEnabled: function() {
		    var isIOS, isAndroid;

		    isIOS = this.operatingSystem.toLowerCase() == "iphone os";
		    isAndroid = !isIOS;

		    if (isIOS) {
			    return this.guidedAccessEnabled ? false : true;
		    } else if (isAndroid) {
			    return false;
		    }
		    return true;
	    },

	    /** Check if the device supports a given feature.
	     *  <br><br>*NOTE This does not indicate if the feature is enabled, only
	     *  that we expect the device to support it.
	     *  The result is based off of the operating system reported by the device,
	     *  eg. iphone OS supports guided access mode, Android does not.<br><br>
	     *
	     *  Valid feature values:<br>
	     *
	     *  <ul>
	     *  <li>'guided_access' - Guided Access Mode</li>
	     *  <li>'text_to_speech' - Text To Speech (Accessibility Detection)</li>
	     *  <li>'running_processes' - Running Processes</li>
	     *  <li>'installed_packages' - Installed Packages</li>
	     *  <li>'audio_recording' - Audio Recording</li>
	     *  <li>'mic_mute' - Mice Muting</li>
	     *  <li>'tts_engine' - Text To Speech (Speech Synthesis)</li>
	     *  <li>'exit' - Exit the app via javascript
	     *  </ul>
	     *  @param {String} feature the feature to check for compatibility.
	     *  @return {Boolean} true if the device supports the feature, false otherwise.
	     */
	    supportsFeature: function(_feature) {
		    var isIOS, isAndroid;

		    isIOS = this.operatingSystem.toLowerCase() == "iphone os";
		    isAndroid = this.operatingSystem.toLowerCase() == "android";

		    if (_feature == "guided_access") /* Only ios */
		    {
			    return isIOS;
		    } else if (_feature == "text_to_speech") /*either */
		    {
			    return isIOS || isAndroid;
		    } else if (_feature == "running_processes") /*either*/
		    {
			    return isIOS || isAndroid;
		    } else if (_feature == "installed_packages") /*android */
		    {
			    return isAndroid;
		    } else if (_feature == "mic_mute") /*android*/
		    {
			    return isAndroid;
		    } else if (_feature == "tts_engine") /*either*/
		    {
			    return isAndroid || isIOS;
		    } else if (_feature == "exit") /*android*/
		    {
			    return isAndroid;
		    }

		    return false;
	    },

	    /** Convenience function for printing the device info.
	     *  @returns {String} a string in the form
	     *  "Device: 'device_name', OS: 'os_name', Version: 'os_version', Jailbroken : 'yes_or_no', Resolution : {width, height}"gui
	     */
	    formattedDeviceInfo: function() {
		    var isiOS = this.operatingSystem.toLowerCase() == "iphone os";

		    return "Device: " + this.model + ", OS: " + this.operatingSystem + ", Version: " + this.operatingSystemVersion + (isiOS ? ", Jailbroken: " : ", Rooted: ") + (this.rootAccess ? "Yes" : "No") + ", Resolution: " + this.screenResolution;
	    }

    },
    
    settings: {
    	
        /** A Timestamp from when the app was launched.
        @type String */
       appStartTime : "unknown"

    },

    /** Dispatch an event using the document; with the given name.
     *
     *  @param eventName the name of the event to dispatch
     *  @return true if the event was dispatched, false if not.
     *  @private
     */
    dispatchEvent: function(_eventName) {
	    var result, event;

	    result = false;

	    if (_eventName && _eventName.length > 0) {
		    event = document.createEvent("Event");
		    event.initEvent(_eventName, true, true);
		    document.dispatchEvent(event);
		    result = true;
	    }

	    return result;
    },

    dispatchMessageEvent: function(_eventName, _message) {
	    var result, origin, event;

	    result = false;

	    if (_eventName && _eventName.length > 0) {
		    /*CustomEvent would work better here*/
		    origin = window.location.protocol + "//" + window.location.host;
		    event = document.createEvent("MessageEvent");
		    event.initMessageEvent(_eventName, true, true, _message, origin, 12, window, null);
		    document.dispatchEvent(event);
		    result = true;
	    }

	    return result;
    },

    /** Executes the callback corresponding to the response object provided.
     *
     *  @param {Object} responseObject the parsed response object.
     *  @config {String} identifier the original request identifier.
     *  @return true if a callback was found, false if no callback was found.
     *  @private
     */
    executeCallback: function(_responseObject, identifier, persistent) {
	    var callback, result = false;

	    if (identifier != null) callback = this.callbacks[identifier];

	    if (callback != null) {
	        if (persistent != true) {
	            delete this.callbacks[identifier];
	        }
		    callback(_responseObject);
		    result = true;
	    }

	    return result;
    },
    
    /** Send a message to a native ios app via its url loading mechanism.
     *  The url that's loaded takes the form key:##airMobile_msgsnd##value, where key and value are parsed out of the url in the application. <br><br>
     *  Note* this mechanism will only work with the native counterpart of <code>AIRMobile</code>.
     *
     *  In our context, the <code>key</code> passed should be one of a known set of commands that the native
     *  application understands. Additionally, the <code>value</code> parameter should be in a format that the
     *  application expects and can understand.
     *  When applicable, the <code>value</code> parameter will typically be a JSON string with additional info about what the command should do.
     *
     *  @param {String} key the key to pass the application
     *  @param {String} value the value to pass for the key
     *  @private
     */
    sendToApp: function(_key, _value) {
	    var iframe = document.createElement("IFRAME");
	    iframe.setAttribute("src", _key + ":##airMobile_msgsnd##" + _value);
	    document.documentElement.appendChild(iframe);
	    iframe.parentNode.removeChild(iframe);
	    iframe = null;
    },

    /** Send a command, with a callback and identifier, to the native application.
     *  <br><br>*This function is intended to be used internally by AIRMobile.
     *  @param {String} command the command to send
     *  @param {String} identifier a unique identifier for the request, or null to generate one.
     *  @param {Function} callback the callback to register
     *  @return {String} identifier the identifier used
     */
    sendCommand: function(_command, _identifier, _callback) {
	    var params = null;

	    if (_callback != null) {
		    _identifier = _identifier == null ? this.UUID() : _identifier;

		    this.callbacks[_identifier] = _callback;

		    params = {
			    identifier: _identifier
		    };
	    }
	    
	    this.sendToApp(_command, JSON.stringify(params, null, true));

	    return _identifier;
    },

    /** Utility function to generate a unique identifier. Users can call
     *  this to generate an identifier for request methods.
     *  @return {String} a unique string
     */
    UUID: function() {
	    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
		    var r = Math.random() * 16 | 0,
			    v = c == 'x' ? r : (r & 0x3 | 0x8);
		    return v.toString(16);
	    });

    },

    /** Convenience function to add a listener to an event.
     *  Supports W3C and IE event mechanisms.
     *  @param {String} event the name of the event to listen for.
     *  @param {Object} element the element that will fire the event.
     *  @param {Function} function the function to call when the event fires.
     */
    listen: function (evnt, elem, func, disposeAfterFired) {
        if (disposeAfterFired === true) {
            var handler = func;
            func = function () {
                /* invoke the actual handler */
                var args = [].slice.call(arguments);
                handler.apply(this, args);

                /* remove this handler */
                if (elem.removeEventListener) {
                    elem.removeEventListener(evnt, func);
                }
                else if (elem.detachEvent) {
                    elem.detachEvent('on' + evnt, func);
                }
            };
        }

	    if (elem.addEventListener) /* W3C DOM */
	    {
		    elem.addEventListener(evnt, func, false);
	    } else if (elem.attachEvent) /* IE DOM */
	    {
		    return elem.attachEvent("on" + evnt, func);
	    }
    },
    
    events: {

        EVENT_KEYBOARD_CHANGED: 'event_keyboard_changed',
        EVENT_CLIPBOARD_CHANGED: 'event_clipboard_changed',
        EVENT_MINI_APP_DETECTED: 'event_mini_app_detected',
        EVENT_ENTER_BACKGROUND: 'event_enter_background',
        EVENT_RETURN_FROM_BACKGROUND: 'event_return_from_background',

        addEventListener: function (_event, _callback) {
            if (_callback != null && 
                (_event === this.EVENT_KEYBOARD_CHANGED
                || _event === this.EVENT_CLIPBOARD_CHANGED
                || _event === this.EVENT_MINI_APP_DETECTED
                || _event === this.EVENT_ENTER_BACKGROUND
                || _event === this.EVENT_RETURN_FROM_BACKGROUND)) {
                /*  store the callback */
                AIRMobile.eventsCallbackId.set(_event, _callback);
            }
        },

    },
    
    ntvOnEventDispatched: function (_parameters) {
        if (_parameters != null) {
            var results = JSON.parse(_parameters, null);
            if (results != null && results.event != null) {
            	if (results.event === AIRMobile.events.EVENT_KEYBOARD_CHANGED) {
            		AIRMobile.security.isKeyboardChanged = true;
            	} else if (results.event === AIRMobile.events.EVENT_CLIPBOARD_CHANGED) {
            		AIRMobile.security.isClipboardChanged = true;
            	} else if (results.event === AIRMobile.events.EVENT_MINI_APP_DETECTED) {
            		AIRMobile.security.isMiniappDetected = true;
            	} else if (results.event === AIRMobile.events.EVENT_ENTER_BACKGROUND) {
            		AIRMobile.security.isEnterBackgroundFired = true;
            	} else if (results.event === AIRMobile.events.EVENT_RETURN_FROM_BACKGROUND) {
            		AIRMobile.security.isReturnFromBackgroundFired = true;
            	};
                if (AIRMobile.eventsCallbackId.has(results.event)) {
                    var callbackFunc = AIRMobile.eventsCallbackId.get(results.event);
                    delete results.event;
                    callbackFunc(results);
                }
            }
        }
    },
    
    /** Request that the device initialize itself and report its status.
     *  This function is automatically invoked when the window's load event
     *  is fired. It is unneccessary to call this manually.<br><br>
     *  If the device is compatible, it will respond by making a corresponding
     *  call to <code>AIRMobile.ntvOnDeviceReady()</code>, at which point, the
     *  device info will be set up, and the <code>AIRMobile.EVENT_DEVICE_READY</code>
     *  event is fired on the document.
     */
    initialize: function() {
	    var params = {
		    version: "2.0"
	    };

	    this.sendToApp(this.CMD_INITIALIZE, JSON.stringify(params, null, true));
    },
    
    /** Called when the device is ready; in response to an 'cmdInitialize' request.
     *
     *  @param {String} Parameters JSON representation of the device info.
     *  @config {String} model
     *  @config {String} operatingSystem
     *  @config {Number} operatingSystemVersion
     *  @config {Number} apiVersion
     *  @config {Array{String}} runningProcesses
     *  @config {Array{String}} [installedPackages]
     *  @config {Boolean} textToSpeech
     *  @config {Boolean} guidedAccess
     *  @config {Boolean} rootAccess
     *  @config {String} connectivity
     *	@config {String} screenResolution
     *	@config {String} orientation
     *  @config {String} lockedOrientation
     *  @config {String} ttsEngineStatus
     *  @return {String} 'OK'
     */
    ntvOnDeviceReady: function(_parameters) {
      
	    var results = JSON.parse(_parameters, null);

	    this.device.isReady = true;
	    this.device.apiVersion = results.apiVersion != null ? results.apiVersion : -1.0;
	    this.device.model = results.model;
	    this.device.manufacturer = results.manufacturer;
	    this.device.operatingSystem = results.operatingSystem;
	    this.device.operatingSystemVersion = results.operatingSystemVersion;
	    this.device.runningProcesses = results.runningProcesses;
	    this.device.installedPackages = results.installedPackages;
	    this.device.rootAccess = results.rootAccess;
	    this.device.connectivityStatus = results.connectivity;
	    this.device.defaultURL = results.defaultURL;
	    this.device.textToSpeechEnabled = results.textToSpeech;
	    this.device.guidedAccessEnabled = results.guidedAccess;
	    this.device.screenResolution = results.screenResolution;
	    this.device.orientation = results.orientation;
	    this.device.lockedOrientation = results.lockedOrientation;
	    this.device.ttsEngineStatus = results.ttsEngineStatus;
	    this.device.keyboard = results.keyboard;
	    this.device.micMuted = results.muted;
	    this.device.ipAddress = results.ipAddress;
	    this.settings.appStartTime = results.startTime;
        this.device.macAddress = results.macAddress;
        this.device.ttsVoices = results.availableTTSLanguages;
        if (results.ttsSettings != null) {
            this.device.ttsPitch = results.ttsSettings.pitch;
            this.device.ttsRate = results.ttsSettings.rate;
            this.device.ttsVolume = results.ttsSettings.volume;
        }

    },
    
    /** Called when The native TextToSpeech engine is enabled on the device, or in response
     *  to a request to check the status of TextToSpeech.
     *	@param Parameters JSON representation of the result
     *	@config {Boolean} enabled (status of the accessibility)
     *	@config {String} ttsEngineStatus ('unavailable', 'playing', 'idle')
     *  @return the status of textToSpeechEnabled.
     */
    ntvOnTextToSpeechEnabled: function(_parameters) {
	    var results, changed;

	    results = JSON.parse(_parameters, null);
	    changed = this.device.textToSpeechEnabled != results.enabled;

	    if (changed == false) {
		    changed = this.device.ttsEngineStatus != results.ttsEngineStatus;
	    }

	    this.device.textToSpeechEnabled = results.enabled;

	    if (results.ttsEngineStatus != null) {
		    this.device.ttsEngineStatus = results.ttsEngineStatus;
	    }

	    var identifier = results.identifier;
	    delete results.identifier;

	    this.executeCallback(this.device.ttsEngineStatus, identifier, false);

	    return this.device.textToSpeechEnabled;
    },
    
    onTTSSynchronized: function (_parameters) {
        var results, callback;

        results = JSON.parse(_parameters, null);

        if (AIRMobile.ttsCallbackId != null) {
        	callback = AIRMobile.callbacks[AIRMobile.ttsCallbackId];
        }

        if (callback != null) {
            callback(results);
        }
    },
    
    /** Called when the device has updated its list of running processes.
     *
     *  @param Parameters JSON respresentation of the result
     *  @config {String} [identifier]
     *  @config {Array{String}} runningProcesses
     *  @return {Boolean} true if there was a change, false if not.
     */
    ntvOnRunningProcessesUpdated: function(_parameters) {
	    var results, processes, added, removed;

	    results = JSON.parse(_parameters, null);
	    processes = results.runningProcesses;
	    added = diffArr(processes, this.device.runningProcesses);
	    removed = diffArr(this.device.runningProcesses, processes);

	    this.device.runningProcesses = processes;
    },
    
    /** Called when the application has updated its default url.
     *  @param {String} Parameters JSON representation of the result
     *  @config {String} url
     */
    ntvOnSetDefaultURL: function(_parameters) {
	    var results;

	    results = JSON.parse(_parameters, null);

	    if (results.url) {
		    AIRMobile.device.defaultURL = results.url;
	    }
    },
    
    /** Called when the application has restored its default url to the original value.
     *  @param {String} Parameters JSON representation of the result
     *  @config {String} url
     */
    ntvOnRestoreDefaultURL: function(_parameters) {
	    var results;

	    results = JSON.parse(_parameters, null);

	    if (results.url) {
		    AIRMobile.device.defaultURL = results.url;
	    }
    },

    /** @namespace Represents security methods for the device */
    security : {
    	 
    	isKeyboardChanged: false,    
    	isClipboardChanged: false,   	
    	isMiniappDetected: false,    	
    	isEnterBackgroundFired: false,    	
    	isReturnFromBackgroundFired: false,

        isEnvironmentSecure: function (_callback) {
        	results = { 'secure': 'true', 'messageKey': '' };
        	if (this.isKeyboardChanged) {
        		results.secure = 'false';
        		results.messageKey = 'Android Keyboard Changed. ';
        	}
        	
        	if (this.isClipboardChanged) {
        		results.secure = 'false';
        		results.messageKey += 'Android device clipboard content Changed. ';
        	}
        	
        	if (this.isMiniappDetected) {
        		results.secure = 'false';
        		results.messageKey += 'Mini App has been invoked. ';
        	}
        	
        	if (this.isEnterBackgroundFired) {
        		results.secure = 'false';
        		results.messageKey += 'The browser has been pushed to background. ';
        	}
        	if (this.isReturnFromBackgroundFired) {
        		results.secure = 'false';
        		results.messageKey += 'The browser has been returned from background.';
        	}
        	
        	if (_callback != null && typeof _callback == 'function') {
        		_callback(results);
        	}
        },

        setAltStartPage: function (_url) {
            if (_url != null && _url != '') {
            	var params = {
				    url: _url
			    };

            	AIRMobile.sendToApp(AIRMobile.CMD_SET_DEFAULT_URL, JSON.stringify(params, null, true));
            }
        },

        restoreDefaultStartPage: function () {
		    var params = { };

		    AIRMobile.sendToApp(AIRMobile.CMD_SET_DEFAULT_URL, JSON.stringify(params, null, true));
        },

        examineProcessList: function (_blacklistedProcessList, _callback) {

        	if (_blacklistedProcessList != null && _callback != null && typeof _callback == 'function') {
        		var result = _blacklistedProcessList.filter(function(n) {
        		    return (AIRMobile.device.runningProcesses.indexOf(n) != -1);
        		});
        		_callback(result);
        	}
        },
        
        getMACAddress: function (_callback) {
        	if (_callback != null && typeof _callback == 'function') {
        		var result = [];
        		if (AIRMobile.device.macAddress != null && AIRMobile.device.macAddress != 'unknown') {
        			result.push(AIRMobile.device.macAddress);
        		}
        		_callback(result);
        	}
        },
        
        /** This function is a placeholder and does not perform any actions. It's presence is
         * only to comply with API certification.
         */
        emptyClipBoard: function () {
        	
        },
        
	    getCapability: function(_feature) {
	    	
	    	var result = { };

	    	if (_feature == null || _feature == '') {
	    		return result;
	    	} else if (_feature == "text_to_speech") {
			    return { _feature: true };
		    } else {
		    	return { _feature: false };
		    }
	    },

        /** This function is a placeholder and does not perform any actions. It's presence is
	     * only to comply with API certification.
	     */
	    setCapability : function(_feature, _value, _callbackSuccess, _callbackError) {
	    	/* Do nothing, this function is intentionally left blank. */
	    },
	        
        /** This function is a placeholder and does not perform any actions. It's presence is
        * only to comply with API certification.
        */
        lockDown : function(enable, _callbackSuccess, _callbackError) {
    	    /* Do nothing, this function is intentionally left blank. */
        },

        /** Requests that the app close
        * Android only
        * @param {boolean} _restart application will restart after closing if true
 	    */
        close : function(_restart) {
            if (!_restart) {
            	return AIRMobile.sendCommand(this.CMD_EXIT_APPLICATION, null, null);
            } else {
            	return AIRMobile.sendCommand(this.CMD_RESTART_APPLICATION, null, null);
            }
        },

        /** Retrieve device details. This method is called to retrieve detailed information about a device.
        * The returned data includes teh manufacturer name, device model number (with hardware revision number if available),
        * and OS version (major, minor, and build number). The format of this string is '<key>=<value>' pairs seperated by the '|'
        * symbol. The valid keys are 'Manufacturer', 'HWVer', and 'SWVer'.
        */
        getDeviceInfo: function (_callback) {

            if (_callback != null && typeof _callback == 'function') {
                var deviceInfo = {
                    'Manufacturer': AIRMobile.device.manufacturer,
                    'HWVer': AIRMobile.device.model,
                    'SWVer': AIRMobile.device.operatingSystemVersion
                };

                _callback(deviceInfo);
            }
        }

    },

    /** @namespace Represents text to speech functions for the device */
    tts : {
    	
    	pauseResumeEnabled: false,
   
        speak: function(_textToSpeak, _options, _callback) {

            var params = null;
            var _identifier = null;
        
            if (_textToSpeak != null) {
            	
            	if (_callback != null && typeof _callback == 'function') {
                    _identifier = AIRMobile.UUID();
            
                    AIRMobile.callbacks[_identifier] = _callback;
                    AIRMobile.ttsCallbackId = _identifier;
            
                    params = {
                        identifier: _identifier,
                        pauseResumeEnabled: this.pauseResumeEnabled,
                        textToSpeak: _textToSpeak
                    };

                } else {
                    params = {
                        pauseResumeEnabled: this.pauseResumeEnabled,
                        textToSpeak: _textToSpeak
                    };
                }

                if (_options != null && typeof _options == 'object') {
                    params.options = _options;
                }

                AIRMobile.sendToApp(AIRMobile.CMD_SPEAK_TEXT, JSON.stringify(params, null, true));
            }
        },
    
        stop: function(_callback) {
        	if (_callback != null && typeof _callback == 'function') {
        		return AIRMobile.sendCommand(AIRMobile.CMD_STOP_SPEAKING, null, _callback);
        	}
        },
    
        getStatus: function(_callback) {
        	if (_callback != null && typeof _callback == 'function') {
        		return AIRMobile.sendCommand(AIRMobile.CMD_CHECK_TTS, null, _callback);
        	}
        },

        pause: function (_callback) {
            if (this.pauseResumeEnabled) {
            	return AIRMobile.sendCommand(AIRMobile.CMD_PAUSE_SPEAKING, null, null);
            }
        },

        resume: function (_callback) {
        	if (this.pauseResumeEnabled) {
        		return AIRMobile.sendCommand(AIRMobile.CMD_RESUME_SPEAKING, null, null);
        	}
        },

        getVoices: function(_callback) {
        	if (_callback != null && typeof _callback == 'function') {
        		if (AIRMobile.device.ttsVoices != null && AIRMobile.device.ttsVoices != "unknown") {
        			_callback(AIRMobile.device.ttsVoices);
        		} else {
        			var voices = [];
        			_callback(voices);
        		}
        	}
        }
    },

    recorder: {

        initialize: function (_callback) {
        	
        },

        getCapabilities: function (_callback) {

        },

        getStatus: function (_callback) {

        },

        startCapture: function (_options, _callback) {

        },

        stopCapture: function () {

        },

        retrieveAudioFileList: function (_callback) {
        	
        },

        retrieveAudioFile: function (filename, _callback) {

        },

        removeAudioFiles: function (_callback) {

        },

        play: function (audioInfo, _callback) {

        },

        stopPlay: function () {

        },

        pausePlay: function () {

        },

        resumePlay: function () {

        }
    }

};

/********************** CODE FROM SUMMIT   ********************/
window.SecureBrowser = AIRMobile;

SecureBrowser.initialize();
