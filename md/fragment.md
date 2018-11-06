在上一篇*学习安卓开发[1]-程序结构、Activity生命周期及页面通信*中，学习了Activity的一些基础应用，基于这些知识，可以构建一些简单的APP了，但这还远远不够，本节会学习如何使用Activity托管Fragment的方式来进行开发

[TOC]

#### 为什么需要Fragment
##### 单纯使用Activity的局限
为什么需要Fragment呢，这要从Activity的局限说起。在前面使用Activity的过程中已经发现，Activity很容易被销毁重建，甚至是在设备旋转的时候也会被销毁，为了返回之前的状态需要保存各种界面相关的信息。
再来假设一种比较常见的场景，一个列表界面+明细界面构成的应用，如果用两个Activity来实现也可以，但如果用户在平板设备上运行应用，则最好能同时显示列表和明细记录，类似网易云、QQ那样在屏幕左侧约1/3的区域显示列表，右侧剩余的区域展示详细信息，这是使用两个Activity无法满足的；另外，查看能否在用户想查看下一条明细时不必回退、再点击进入明细界面，而是采用在屏幕横向滑动切换到下一条这样的快捷手势呢，这也是两个Activity无法满足的。

##### Fragment介绍
接下来该是Fragment隆重登场的时候了，可以说Fragment就是为了应对UI的灵活需求而生的，Fragment是在API 11中开始引入的，当时Google发布了第一台平板设备。
那么什么是Fragment呢，Fragment是一种控制器对象，可以在Activity的托管下进行用户界面的管理，受其管理的界面可以是整个屏幕区域，也可以是一小部分，Fragment（碎片）就是这个意思。
要让Activity能够托管Fragment，则需要activity视图预留fragment插入其中的位置。一个activity视图中可以插入过个fragment视图。Fragment本身没有在屏幕上显示视图的能力，所以它必须放置在Activity的视图层级中。

#### 如何使用Fragment
##### 代码实现
- 定义容器视图
sd
- 创建布局文件
- 创建Fragment类
- 向FragmentManager中添加Fragment

生命周期
具体托管实现