/**
 */
var expect = require("chai").expect;

var runGams = require('../agents/RunGAMSPredefinedOwlFile')






describe('runGAMS', function () {
    it('delete old files', function (done) {
        
        runGams(0, function (err, result) {
            console.log(result)
            
        })
        
        
    })
})