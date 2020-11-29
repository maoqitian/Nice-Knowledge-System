
//创建 XmlSlurper 类

def xmlspr = new XmlSlurper()

//获取清单文件 file

def file = new File("AndroidManifest.xml")

//获取解析对象
//获取清单文件根元素，也就是 manifest 标签
def manifest = xmlspr.parse(file)

// 声明命名空间
//manifest.declareNamespace('android':'http://schemas.android.com/apk/res/android')
//获取包名
println manifest.'@package'

//获取 activity intent-filter

def activity = manifest.application.activity
//获取 intent-filter 设置的过滤条件 也可以此判断是否为应用程序入口 activity
activity.find{
   it.'intent-filter'.find { filter ->
       filter.action.find{
           println it.'@android:name'.text()
       }
       filter.category.find{
           println it.'@android:name'.text()
       }
   }
}

