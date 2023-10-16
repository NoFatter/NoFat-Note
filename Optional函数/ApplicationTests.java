package org.nofat.robot;

import org.junit.jupiter.api.Test;
import org.nofat.robot.domain.Result;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;

@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void optionalTest(){
        Result result = new Result().getResultDemo();
        //一般写法
        if(result!=null){
            Map<String,String> data = result.getData();
            if(data!=null){
                String url = data.get("url");
            }
        }

        //empty()函数:返回一个Empty对象
        Optional<Result> emptyOptional = Optional.empty();
        System.out.println(emptyOptional);
        //of()函数:创建一个非空的Optional对象
        //如果参数为空,则会抛出NullPointerException
        Optional<Result> optionalByOf = Optional.of(new Result());
        System.out.println(optionalByOf);
        //ofNullable函数:如果参数为空,返回Empty对象,避免抛出NullPointerException
        Optional<Result> optional = Optional.ofNullable(new Result().getResultDemo());
        Optional<Result> optionalNull = Optional.ofNullable(new Result().getNullResult());
        System.out.println(optional);
        System.out.println(optionalNull);

        //isPresent()判断是否存在,存在返回true,不存在返回false
        if(optional.isPresent()){
            System.out.println("optional不为空！");
        }else {
            System.out.println("optional为空！");
        }
        if(optionalNull.isPresent()){
            System.out.println("optional不为空！");
        }else {
            System.out.println("optional为空！");
        }

        //ifPresent()方法:存在则操作
        optional.ifPresent(op -> {
            if(op.getCode()==0){
                System.out.println(op.getMessage());
            }
        });

        //orElse与orElseGet方法:当optional对象为empty时,执行操作
        Result testOrElse = null;
        testOrElse = Optional.ofNullable(testOrElse).orElse(new Result().getResultDemo());
        testOrElse = Optional.ofNullable(testOrElse).orElseGet(()->new Result().getResultDemo());
        //orElseThrow:当optional对象为empty时,抛出异常
        testOrElse = Optional.ofNullable(testOrElse).orElseThrow();

        //当我们需要拿到result对象中里面的data对象时,我们就可以用到map方法
        Optional<Map<String,String>> data = Optional.of(new Result().getResultDemo()).map(Result::getData);
        data.ifPresent(System.out::println);
    }

}
