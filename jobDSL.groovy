/*
	Parameters:
	BASE_NAME		The name the jobs will be getting 	(ex.: my_plugin)
	REPO			The project repository				(ex.: git@github.com/me/myPlugin.git)
	BRANCH			The branches to build				(ex.: ready**)
*/

BRANCH="master"
BASE_NAME="memory-map-examples"
REPO="https://github.com/Praqma/memory-map-examples.git"

/*
def branchApi = new URL("https://api.github.com/repos/${REPO}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())
branches.each {
    def branchName = it.name
    job {
        name "${project}-${branchName}".replaceAll('/', '-')
        scm {
            git("git://github.com/${project}.git", branchName)
        }
    }
}
*/


// TODOD
// - delete jobs before running, all those for branches, to only have 5 build when user looks at them
// try also for another branch




freeStyleJob("${BASE_NAME}_demo-runner_GEN") {
    description('FIXME ')
    label 'linux'
/*    logRotator { // since 1.35
        daysToKeep(-1)
        numToKeep(5)
        artifactDaysToKeep(int artifactDaysToKeep)
        artifactNumToKeep(int artifactNumToKeep)
    }
*/
  
    parameters {
      stringParam("EXAMPLE_BRANCH", "arm-none-eabi-gcc_4.8.4_hello_world_update1",
                    "Which example branch to run the demo for?")
    }
    scm {
        git {
            remote {
                url("${REPO}")
            }
            branch('${BRANCH}')
        }

    }
  	properties {
        environmentVariables {
          	keepSystemVariables(true)
          	keepBuildVariables(true)
            env('BRANCH', "${BRANCH}")
            env('REPO', "${REPO}")
            env('BASE_NAME', "${BASE_NAME}")
        }
  	}
    steps {
        shell('''
#!/bin/bash

# FIXME - should be a demo-runner.sh script on master


# get number of commits to build in the example

COMMIT_COUNT=`git log --format=format:%H --reverse first_$EXAMPLE_BRANCH^1..last_$EXAMPLE_BRANCH | wc -w`


# FIXME - get a job.dsl file from the branch, then create the new job before running it

echo "COMMIT_COUNT=$COMMIT_COUNT" > $JOB_NAME.properties
echo "EXAMPLE_BRANCH=${EXAMPLE_BRANCH}" >> $JOB_NAME.properties
echo "BASE_NAME=${BASE_NAME}" >> $JOB_NAME.properties
echo "REPO=${REPO}" >> $JOB_NAME.properties


cat $JOB_NAME.properties

''')
        
        environmentVariables {
            propertiesFile('$JOB_NAME.properties')
            env('JOB_DSL_FILE', 'examples/${EXAMPLE_BRANCH}/jenkins-job-dsl.groovy')
        }
      
      
        dsl {
          //external('${JOB_DSL_FILE}')
          external('examples/**/jenkins-job-dsl.groovy')
            removeAction('DELETE')
        }
      /*
        conditionalSteps {
            condition {                      // only one condition is allowed
                alwaysRun()                  // run no matter what
                expression('${EXAMPLE_BRANCH}', '${EXAMPLE_BRANCH}')
                runner('Unstable')
              steps { // one or more build steps, since 1.35
    	          downstreamParameterized {
                    trigger('${BASE_NAME}_${EXAMPLE_BRANCH}_GEN', 'SUCCESS', false,
            	        [buildStepFailure: 'FAILURE',
                	     failure         : 'FAILURE',
                    	 unstable        : 'UNSTABLE']) {
                			propertiesFile('$JOB_NAME.properties', true)
              		     }
              		}
              
                  }
            }
    	}*/
      downstreamParameterized {
        	        trigger('${BASE_NAME}_${EXAMPLE_BRANCH}_GEN', 'SUCCESS', false,
            	        [buildStepFailure: 'FAILURE',
                	     failure         : 'FAILURE',
                    	 unstable        : 'UNSTABLE']) {
                			propertiesFile('$JOB_NAME.properties', true)
              		     }
              		}
    }
       configure { project -> 
         
/*        project / 'builders' / 'org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder' \
         / 'conditionalbuilders' / 'hudson.plugins.parameterizedtrigger.TriggerBuilder' \*/
         project / 'builders' / 'hudson.plugins.parameterizedtrigger.TriggerBuilder' \
         / 'configs' \
         / 'hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig'  << 
           configFactories {
             'hudson.plugins.parameterizedtrigger.CounterBuildParameterFactory' {
               from '1' 
               to '${COMMIT_COUNT}'
               step '1' 
               paramExpr('''
CURRENT_COMMIT=${COUNT}''')
               validationFail 'FAIL'
             }
        } 
      }
}
