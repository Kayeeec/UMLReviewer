package org.umlreviewer.sync

enum class RemoteRepo {
    NONE, GIT;

    companion object {
        val default = NONE
    }
}
