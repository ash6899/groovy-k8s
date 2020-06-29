job("job-1"){
	label('master')
	scm {
		github('ash6899/jenkins-k8s-automation','master')
	}
	 triggers {
    		pollSCM {
			scmpoll_spec("* * * * *")
			ignorePostCommitHooks(true)
		}
 	 }

	steps {
		shell(''' version=`cat version`
			docker build -t yash6899/httpd-centos:$version .
			docker login -u="yash6899" -p="password"
			docker push yash6899/httpd-centos:$version ''')
	}
}

job("job-2"){
	label('docker')
	scm {
		github('ash6899/jenkins-k8s-automation','master')
	}
	triggers {
		upstream('job-1','SUCCESS')
	}
	steps {
		shell(''' version=`cat version`
		if kubectl get service/webapp
		then
		kubectl set image deploy webapp myapp=yash6899/httpd-centos:$version
		else
		kubectl create -f testdeploy.yaml
		fi ''')
	}
}

job("job-3"){
	label('docker')
	scm {
		github('ash6899/jenkins-k8s-automation','master')
	}
	triggers {
		upstream('job-2','SUCCESS')
	}
	steps {
		shell(''' status=$(curl -o /dev/null -s -w "%{http_code}" 192.168.99.101:30022/index.html)
		if [[ $status == 200 ]]
		then
		python3 pass.py
		else
		python3 fail.py
		fi ''')
	}
}

buildPipelineView('groovy pipeline') {
	title('task-6-devops')
	displayedBuilds(2)
	selectedJob('job-1')
	showPipelineParameters(true)
	refreshFrequency(5)
}