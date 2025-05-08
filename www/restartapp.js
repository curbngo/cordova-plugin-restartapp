var exec = require('cordova/exec');

var RestartApp = {
    go: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'RestartApp', 'restartApp', []);
    }
};

module.exports = RestartApp;

if (typeof window !== 'undefined' && !!window.cordova) {
    window.RestartApp = RestartApp;
}