from fabric.decorators import task
from fabric.contrib    import project
from fabric.api        import local, sudo

@task
def build():
    local('play clean compile stage')

@task
def rsync():
    project.rsync_project(local_dir='target', remote_dir='/usr/local/milmsearch')

@task
def restart():
    sudo('supervisorctl restart milmsearch', pty=True)
