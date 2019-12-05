package com.xx.module.video.model.entity;

import com.xx.lib.db.entity.MainVideo;

import java.util.List;

/**
 * @author someone
 * @date 2019-06-05
 */
public class VideoDetail {

    /**
     * obj1 : {"clicks":1,"coverurl":"http://puui.qpic.cn/qqvideo_ori/0/b05309jjec2_228_128/0","createtime":{"date":29,"day":3,"hours":16,"minutes":52,"month":4,"seconds":20,"time":1559119940000,"timezoneOffset":-480,"year":119},"isadv":0,"iscollection":0,"ispraise":0,"ispush":0,"praise":3,"sharemsg":"成龙这部电影被吐槽成烂片 却狂赚8亿","sharenum":6,"sharephoto":"http://puui.qpic.cn/qqvideo_ori/0/b05309jjec2_228_128/0","shareurl":"https://v.qq.com/iframe/player.html?vid=b05309jjec2&tiny=0&auto=0","timestr":"2019年05月29日 16:52:20","typeid":1,"uid":100004,"userimg":"","username":"神奇的皮皮虾","vid":2,"videolength":222,"videomsg":"","videotitle":"成龙这部电影被吐槽成烂片 却狂赚8亿","videourl":"https://v.qq.com/iframe/player.html?vid=b05309jjec2&tiny=0&auto=0"}
     * obj2 : [{"username":null,"clicks":2,"uid":100004,"createtime":1559119822000,"sharenum":5,"sharemsg":"曼谷国际车展漂亮的车模","sharephoto":"http://puui.qpic.cn/qqvideo_ori/0/s0562ow8bct_228_128/0","shareurl":"https://v.qq.com/iframe/player.html?vid=s0562ow8bct&tiny=0&auto=0","vid":1,"praise":7,"typeid":1,"timestr":null,"videomsg":null,"userimg":null,"videolength":59,"videotitle":"曼谷国际车展漂亮的车模","videourl":"https://v.qq.com/iframe/player.html?vid=s0562ow8bct&tiny=0&auto=0","coverurl":"http://puui.qpic.cn/qqvideo_ori/0/s0562ow8bct_228_128/0","ispraise":0,"iscollection":0,"isadv":0,"ispush":0},{"username":null,"clicks":36412,"uid":null,"createtime":null,"sharenum":null,"sharemsg":null,"sharephoto":null,"shareurl":null,"vid":null,"praise":null,"typeid":null,"timestr":null,"videomsg":"一个好名字可以给宝宝一生助运，还可以旺父母。","userimg":null,"videolength":null,"videotitle":"宝宝起名","videourl":"https://www.yixueqm.com/zhiming/index.php/home-bbqm-index","coverurl":"https://www.yixueqm.com/zhiming/Public/images/mingli_shouye/bbqm_banner.jpg","ispraise":0,"iscollection":0,"isadv":1,"ispush":null},{"username":null,"clicks":1,"uid":100004,"createtime":1559119940000,"sharenum":6,"sharemsg":"成龙这部电影被吐槽成烂片 却狂赚8亿","sharephoto":"http://puui.qpic.cn/qqvideo_ori/0/b05309jjec2_228_128/0","shareurl":"https://v.qq.com/iframe/player.html?vid=b05309jjec2&tiny=0&auto=0","vid":2,"praise":3,"typeid":1,"timestr":null,"videomsg":null,"userimg":null,"videolength":222,"videotitle":"成龙这部电影被吐槽成烂片 却狂赚8亿","videourl":"https://v.qq.com/iframe/player.html?vid=b05309jjec2&tiny=0&auto=0","coverurl":"http://puui.qpic.cn/qqvideo_ori/0/b05309jjec2_228_128/0","ispraise":0,"iscollection":0,"isadv":0,"ispush":0}]
     */

    private MainVideo obj1;
    private List<VideoRecommened> obj2;

    public MainVideo getObj1() {
        return obj1;
    }

    public void setObj1(MainVideo obj1) {
        this.obj1 = obj1;
    }

    public List<VideoRecommened> getObj2() {
        return obj2;
    }

    public void setObj2(List<VideoRecommened> obj2) {
        this.obj2 = obj2;
    }


}
