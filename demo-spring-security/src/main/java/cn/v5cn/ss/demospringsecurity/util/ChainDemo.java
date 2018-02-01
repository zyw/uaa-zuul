package cn.v5cn.ss.demospringsecurity.util;

import java.util.ArrayList;
import java.util.List;

public class ChainDemo {
    public static void main(String[] args) {
        new IAChainClass().doSomeThing();
    }
}

/**
 * 下面代码是链式操作的代码例子，不使用循环，完成循环调研的功能（间接的递归）
 */
interface IA {
    void doSomeThing(IAChain chain);
}

interface IAChain {
    void doSomeThing();
}

class IAClass implements IA {

    @Override
    public void doSomeThing(IAChain chain) {
        System.out.println("i am IAClass");
        chain.doSomeThing();
    }
}

class IAChainClass implements IAChain {

    private List<IA> iaChain = new ArrayList<>();

    public IAChainClass(){
        iaChain.add(new IAClass());
        iaChain.add(new IAClass());
        iaChain.add(new IAClass());
        iaChain.add(new IAClass());
    }

    private int position = 0;

    @Override
    public void doSomeThing() {
        if(position == iaChain.size()) {
            System.out.println("end");
            return;
        }
        IA ia = iaChain.get(position++);
        ia.doSomeThing(this);
    }
}