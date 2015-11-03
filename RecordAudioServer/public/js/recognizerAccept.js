define(['jquery', 'spoke', 'ractiveUi'],
    function($, spoke, ui) {

        var uiData = ui.data;
        var uiComponent = ui.component;

        var ACCEPT_MATCHING_WORDS_THRESHOLD = 3;
        var MATCHING_PERCENTAGE = 0.60; // Have to get at least 60% of the sentence correct

        var acceptOrRejectSpeech = function (expectedText, recognizedText, index) {
            var expected = spoke.utils.normalizeString(expectedText);
            var recognized = spoke.utils.normalizeString(recognizedText);
            console.log('Expected sentence:', expected);
            console.log('Recognized sentence:', recognized);

            if (expected === recognized) {
                // exactly equal already
                uiData.recognizedState[index] = ui.RecognizedStateEnum.ACCEPTED_PERFECT;
            } else {
                // break into words and count the matches
                expected = expected.split(' ');
                recognized = recognized.split(' ');
                expected.sort();
                recognized.sort();
                var matchCount = 0;
                var matchThreshold = Math.floor(expected.length * MATCHING_PERCENTAGE);
                var j = 0;
                for (var i = 0; i < expected.length; i++) {
                    var matchIndex = recognized.indexOf(expected[i], j);
                    if (matchIndex >= 0) {
                        j = matchIndex + 1;
                        matchCount++;
                    } 
                }
                var matchedPerc = Math.round((matchCount / expected.length) * 100);
                console.log('Matched', matchedPerc, ':', matchCount, 'out of length', expected.length );
                if (matchCount >= matchThreshold) {
                    uiData.recognizedState[index] = ui.RecognizedStateEnum.ACCEPTED_GREAT;
                } else {
                    uiData.recognizedState[index] = ui.RecognizedStateEnum.REJECTED;
                }
            }

            uiComponent.set('recognizedState', uiData.recognizedState);
        };


        return acceptOrRejectSpeech;
});