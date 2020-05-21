import hudson.model.*
def env = Thread.currentThread()?.executable.parent.builds[0].properties.get('envVars')
def map = [:]

if (env['gitlabSourceBranch'] != null) { 
  map['sourceBranch'] = env['gitlabSourceBranch'] 
}
if (env['gitlabTargetBranch'] != null) { 
  map['targetBranch'] = env['gitlabTargetBranch'] 
  map['targetVersion'] = env['gitlabTargetBranch'].split('/')[0] 
}

if (env['gitlabMergeRequestDescription'] == null) {
  map['gitlabMergeRequestDescription'] = ''
}
// Add additional entries for any other parameters you have created

return map