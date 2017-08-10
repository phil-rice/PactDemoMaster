var app = angular.module('demo',[] );
app.controller("PactConsumer", function($scope, $http){
	$scope.postdata = function(postid,authToken){
	    var data = {
			'Authentication-token':authToken
		};
		//alert('data'+data)
		var jsonStr = JSON.stringify(data)
		//alert('jsonStr'+jsonStr)
		var url = "http://localhost:9030/token/id/"+postid
		
		$http({
		 method: 'POST',
		 url: url,
		 data: jsonStr,
		})
		.then( function(response){
		  if( response.data )
			$scope.msg = response.data
			$scope.statusval = response.status;
            $scope.statustext = response.statusText;
            $scope.headers = response.headers();
		  }, function(response){
		    $scope.msg = response.data;
			$scope.statusval = response.status;
            $scope.statustext = response.statusText;
            $scope.headers = response.headers();
		  });
	};
});
