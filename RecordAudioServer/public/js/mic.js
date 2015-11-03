define(['jquery', 'ractiveUi', 'spoke'],
    function($, ui, spoke) {

        var volumeMeterElement = $('.vol-meter');
        var volumeMeter = spoke.microphone.VolumeMeter(volumeMeterElement);

        volumeMeter.adjustVolumeMeter = function (volumeLevel) {
            var adjustedHeight = this._computeMeterHeight(volumeLevel);
            ui.component.set('meterMaxHeight', adjustedHeight + '%');
        };

        return {
            volumeMeterElement: volumeMeterElement,
            volumeMeter: volumeMeter,
        };
});