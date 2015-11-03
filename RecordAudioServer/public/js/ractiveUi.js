define(['jquery', 'ractive'], function($, Ractive) {

    RecognizedStateEnum = {
        UNREAD: 0,
        PENDING: 1,
        ACCEPTED_GREAT: 2,
        ACCEPTED_PERFECT: 3,
        REJECTED: 4,
    };

    var element = $('#taskContainer');
    var template = element.html();
    // Count the number of utterances and initialize the state arrays
    var numUtterances = element.find('.utterance-panel').length;
    var savedState = [], recognizedState = [];
    for(var i = 0; i < numUtterances; i++) {
        recognizedState.push(RecognizedStateEnum.UNREAD);
        savedState.push(false);
    }

    var data = {
        meterMaxHeight: '35%',
        recording: false,
        recordingFragment: -1, // index of the fragment currently being recorded if any
        recognizedState: recognizedState,
        savedState: savedState, //true or false,
        acceptBrowser: true,
        connected: false,
        recDir: 'unset',

        isRecording: function (fragNum) {
            return this.get('recordingFragment') === fragNum;
        },
        hasBeenRead: function (fragNum) {
            return this.get('recognizedState')[fragNum] !== RecognizedStateEnum.UNREAD;
        },
        isPending: function (fragNum) {
            return this.get('recognizedState')[fragNum] === RecognizedStateEnum.PENDING;
        },
        isMatch: function (fragNum) {
            var recState = this.get('recognizedState')[fragNum];
            return (recState === RecognizedStateEnum.ACCEPTED_GREAT || 
                recState === RecognizedStateEnum.ACCEPTED_PERFECT);
        },
        isRejected: function (fragNum) {
            var recState = this.get('recognizedState')[fragNum];
            return (recState === RecognizedStateEnum.REJECTED);
        },
        isAccepted: function (fragNum) {
            return this.get('acceptedState')[fragNum];
        },
        acceptedStateClass: function (fragNum) {
            var recState = this.get('recognizedState')[fragNum];
            var saveState = this.get('savedState')[fragNum];

            switch (recState) {
                case RecognizedStateEnum.UNREAD:
                    return 'invisible';
                case RecognizedStateEnum.PENDING:
                    return 'alert-info';
                case RecognizedStateEnum.ACCEPTED_GREAT:
                case RecognizedStateEnum.ACCEPTED_PERFECT:
                    if (!saveState) {
                        return 'alert-danger';
                    }
                    return 'alert-success';
                case RecognizedStateEnum.REJECTED:
                    return 'alert-danger';
                default:
                    return 'invalid';
            }
        },
        acceptedText: function (fragNum) {
            if (this.get('recognizedState')[fragNum] === RecognizedStateEnum.ACCEPTED_PERFECT) {
                return 'Perfect';
            } else {
                return 'Great';
            }
        },
        
        and: function(a, b) {
            return a && b;
        },
    };

    var computed = {
        acceptedState: function () {
            var acceptedArray = [];
            /* Need to call this property method with call and pass this as a param
            otherwise calls it with this as Window instead of ractive */
            var isMatch = (this.get('isMatch'));
            for(var i = 0; i < numUtterances; i++) {
                acceptedArray[i] = this.get('savedState')[i] && isMatch.call(this, i);
            }
            return acceptedArray;
        },
        allAccepted: function () {
            var self = this;
            var all = this.get('acceptedState').every(function(state, index) {
                return state;
            });
            return all;
        },
        anyPending: function () {
            var ind = this.get('recognizedState').indexOf(RecognizedStateEnum.PENDING);
            if (ind >= 0) {
                return true;
            } else {
                return false;
            }
        },

    };
    
    /* Initialize the Ractive component */
    var ractiveComponent = new Ractive({
        el: element,
        template: template,
        data: data,
        computed: computed,
    });

    element.find('.hiddenOnLoad').removeClass('hidden');

    console.log('Ractive component:', ractiveComponent);

    $('#amtForm').submit(function (event) {
        console.log('Amt form submitted:', event);
    });

    return {
        data: data,
        component: ractiveComponent,
        RecognizedStateEnum: RecognizedStateEnum,
    };

});