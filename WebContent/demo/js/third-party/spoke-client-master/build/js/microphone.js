define(["jquery","sharedAudio","utils"],function(e,t,n){console.log("> Loading microphone.js");var r=t.audioContext,i=r.sampleRate,s=function(t,n){if(!(this instanceof s))return new s(t,n);n=n||{},this.settings=e.extend({},s.DEFAULTS,n),this.$this=e(this),this.element=e(t),this.adjustable=this.element.find(".vol-adjust"),this.autoAdjust=this.settings.autoAdjust,this.adjustableHeight=this.settings.maxMeterHeight-this.settings.minMeterHeight,this.minFreqIndex=this._getFrequencyIndex(this.settings.minFrequency),this.maxFreqIndex=this._getFrequencyIndex(this.settings.maxFrequency),this._setupMeterElement(),this._setupAudioNodes()};return s.DEFAULTS={analyserFftSize:128,smoothingTimeConstant:.5,scriptBufferSize:2048,numInputChannels:1,numOutputChannels:1,autoAdjust:!0,selectable:!0,minMeterHeight:35,maxMeterHeight:100,minFrequency:300,maxFrequency:3300},s.prototype._setupAudioNodes=function(){var e=this;this.analyser=r.createAnalyser(),this.analyser.smoothingTimeConstant=this.settings.smoothingTimeConstant,this.analyser.fftSize=this.settings.analyserFftSize,this.javascriptNode=r.createScriptProcessor(this.settings.scriptBufferSize,this.settings.numInputChannels,this.settings.numOutputChannels),this.javascriptNode.connect(r.destination),this.javascriptNode.onaudioprocess=function(){e._onaudioprocess.call(e)},n.addGlobalVariable("spokeJavascriptNode",this.javascriptNode),console.log("Created analyser node:",typeof this.analyser,this.analyser),console.log("Created script processor node:",typeof this.javascriptNode,this.javascriptNode),t.audioStreamPromise.then(function(t){return console.log("Microphone icon got access to audioStreamSource",typeof t,t),t.connect(e.analyser),e.analyser.connect(e.javascriptNode),e.javascriptNode.connect(r.destination),t}).catch(function(e){console.log("Audio streaming error in microphone:",e)})},s.prototype._setupMeterElement=function(){this.settings.selectable||this.element.addClass("glyph")},s.prototype._onaudioprocess=function(){var e=new Uint8Array(this.analyser.frequencyBinCount);this.analyser.getByteFrequencyData(e);var t=this._computeVolumeLevel(e);this.$this.trigger("volumeLevel.spoke.volumeMeter",[t]),this.autoAdjust&&this.adjustVolumeMeter(t)},s.prototype._getFrequencyIndex=function(e){var t=Math.floor(e*this.settings.analyserFftSize/i);return t},s.prototype._computeMeterHeight=function(e){var t=e*this.adjustableHeight+this.settings.minMeterHeight;t=t.toFixed();var n=this.settings.maxMeterHeight-Math.min(t,this.settings.maxMeterHeight);return n},s.prototype._computeVolumeLevel=function(e){var t=n.average(e,this.minFreqIndex,this.maxFreqIndex),r=t/256;return r},s.prototype.adjustVolumeMeter=function(e){var t=this._computeMeterHeight(e);this.adjustable.css("max-height",t+"%")},s.prototype.on=function(e,t){this.$this.on(e,t)},{VolumeMeter:s}});