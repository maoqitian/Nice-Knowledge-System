# Flutter 瀑布流实现 、可控下拉刷新、上拉加载更多、错误页面展示
> 实现标题所说内容，主要分为了两步，一个是实现瀑布流效果，其次是实现页面可控状态显示

## StaggeredGridView

- 要实现瀑布流，其实Github早有大神提供了轮子[StaggeredGridView](https://pub.dev/packages/flutter_staggered_grid_view)，该轮子既支持自适应宽高的瀑布流，也支持固定宽高的瀑布流

### 使用


```
dependencies:
  flutter_staggered_grid_view: ^0.3.0
```

### StaggeredGridView.countBuilder构架基础瀑布流页面
```
// Grid Widget
  Widget _buildGridWidget(){
    //瀑布流 高度自适应
    return StaggeredGridView.countBuilder(
      primary: false,
      crossAxisCount: 4,
      mainAxisSpacing: 4.0,
      crossAxisSpacing: 4.0,
      controller: _scrollController, //滑动控制器
      itemCount: items.length, // item 数量
      itemBuilder: (BuildContext context, int index) {
        //每个Item 内容构建
        return widget.renderItem(items[index]);
      },

      staggeredTileBuilder: (index) =>
          StaggeredTile.count(2, index.isEven ? 2 : 1)
      //高度自适应 该配置似乎存在滑动卡顿 bug 
      //new StaggeredTile.fit(2),
    );
  }
```

##   实现页面可控状态显示

### 页面构建
```
 @override
  Widget build(BuildContext context) {
    switch (pageStatus){
      case LOAD:
        //加载中
        return _buildIsLoading();
        break;
      case SUCCESS:
        // 数据获取成功
        return _buildPage();
        break;
      case ERROR:
        //页面加载出错
        return _buildEmptyError();
        break;
      default:
        return Container(
          child: new Center(
            child: new Text("暂未实现 Page"),
          ),
        );
        break;
    }
  }
```

- Loading动画效果借助 [flutter_spinkit](https://pub.dev/packages/flutter_spinkit)库


```
  ///上拉加载更多 Widget
  Widget _buildProgressIndicator() {
    return _hasMore
        ? Container(
            child: new Center(
             child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                SpinKitFadingCircle(color: Theme.of(context).primaryColor),
                Padding(
                    child: Text("正在加载..",
                        style:
                            TextStyle(color: Colors.black54, fontSize: 15.0)),
                    padding: EdgeInsets.only(left: 10.0))
              ],
            ),
          )) : Container(
            padding: EdgeInsets.all(15),
            child: new Center(
             child: new Text("哥，这回真没了！！",
                style: TextStyle(color: Colors.black54, fontSize: 15.0)),
          ));
  }

  ///  loading
  Widget _buildIsLoading() {
    return Container(
        width: MediaQuery.of(context).size.width,
        height: MediaQuery.of(context).size.height * 0.85,
        child: new Center(
            child: Column(
             crossAxisAlignment: CrossAxisAlignment.center,
             mainAxisAlignment: MainAxisAlignment.center,
             children: <Widget>[
             Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: <Widget>[
                SpinKitCircle(
                    size: 55.0, color: Theme.of(context).primaryColor,),
              ],
             ),
             Padding(
              child: Text("正在加载..",
                  style: TextStyle(color: Colors.black54, fontSize: 15.0)),
              padding: EdgeInsets.all(15.0),
            )
          ],
        )));
  }

  ///空页面  错误 页面 empty error
  Widget _buildEmptyError() {
    return Container(
      width: MediaQuery.of(context).size.width,
      height: MediaQuery.of(context).size.height * 0.85,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          Text("页面出错了！！"),
          RaisedButton(
            textColor: Colors.white,
            color: Theme.of(context).primaryColor,
            child: Text("重新加载"),
            onPressed: () {
              if (this.mounted) {
                //mounted == true  保证 当前widget 状态可以更新
                setState(() {
                  items.clear();
                  isLoading = false;
                  _hasMore = true;
                  _pageIndex = 0;
                  pageStatus = LOAD;
                });
                _getMoreData();
              }
            },
          )
        ],
      ),
    );
  }
```

- 下拉刷新则使用原生Flutter控件 RefreshIndicator

```
//主界面
  Widget _buildPage() {
    List<Widget> list = [];
    list.add(
      //是否支持下拉刷新
      Expanded(
        child: widget.isCanRefresh ?
        RefreshIndicator(
          onRefresh: _handleRefresh,
          color: Theme.of(context).primaryColor,//指示器颜色
          child: _buildGridWidget())
            : _buildGridWidget()
      )
    );
    if(widget.isCanLoadMore){
       list.add(
         //是否支持 加载更多
         Offstage(offstage: !isLoadMore,
             child: _buildProgressIndicator()),);
    }
    return Column(
      children: list,
    );
  }
```

## 构造函数配置属性


```
 // 模块item
  final renderItem;
  //数据获取方法
  final requestApi;
  //头部
  final headerView;
  //是否添加头部 默认不添加 头部逻辑目前没有添加 TODO
  final bool isHaveHeader;
  //是否支持下拉刷新 默认可以下拉刷新
  final bool isCanRefresh;
  //是否支持上拉加载更多 默认可以加载更多
  final bool isCanLoadMore;

  const RefreshGridPage({
    @required this.requestApi,
    @required this.renderItem,
    this.headerView,
    this.isHaveHeader = false,
    this.isCanRefresh = true,
    this.isCanLoadMore = true,
  })  : assert(requestApi is Function),
        assert(renderItem is Function),
        super();
```

## 最终实现效果

<img src="https://raw.githubusercontent.com/maoqitian/MaoMdPhoto/master/flutter/grid_refresh_page/grid_refresh_load.gif"  height="400" width="230">