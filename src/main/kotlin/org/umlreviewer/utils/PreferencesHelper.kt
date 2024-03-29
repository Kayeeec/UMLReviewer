package org.umlreviewer.utils

import org.umlreviewer.settings.SettingsPreferencesKey
import org.umlreviewer.utils.AppConstants.PREFERENCES_NODE
import java.nio.file.Path
import java.nio.file.Paths
import java.util.prefs.Preferences

object PreferencesHelper {
    fun preferencesHelper(op: Preferences.() -> Unit) {
        val node = Preferences.userRoot().node(PREFERENCES_NODE)
        op(node)
    }

    fun getWorkingDirectory(): Path? {
        var workingDir = ""
        preferencesHelper {
            sync()
            workingDir = get(SettingsPreferencesKey.WORKING_DIRECTORY, "")
        }
        return if (workingDir.isNotEmpty()) Paths.get(workingDir) else null
    }
}

