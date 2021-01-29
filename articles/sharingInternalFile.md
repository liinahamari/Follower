## Sharing file dedicated to internal usage

Imagine you have a log file stored locally, which you want to share with another application (for example, email application). 
[FileProvider](https://developer.android.com/reference/androidx/core/content/FileProvider) is what we need in this case.
In ```AndroidManifest.xml``` you should declare ```<provider>``` section:
```
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:grantUriPermissions="true"
    android:exported="false">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/filepaths" />
</provider>
```

The most important attribute here is ```authorities``` attribute, it should contain unique identifier of your application. ```applicationId``` is a good choice (programmatically you can obtain applicationId 
by calling ```BuildConfig.APPLICATION_ID```). 

In ```res``` directory you should have ```xml``` directory (if it doesn't exist, then create it by your own). In ```xml``` directory you should create file with corresponding name (in our case, it's
filepaths.xml), which should contain elements declaring shareable directories:
```
<paths>
    <files-path name="TempLogs" path="TempLogs/"/>
</paths>
``` 

and therefore, you can expose to other applications all the files you have in declared directories via implicit Intents:
```
Intent(Intent.ACTION_SEND).apply {
    putExtra(Intent.EXTRA_EMAIL, arrayOf(SOME_EMAIL))
    putExtra(Intent.EXTRA_SUBJECT, SOME_MESSAGE_TITLE)
    putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", logFile))
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    type = "text/plain"
}.also { startActivity(it) }
```

