# NPE 问题

NullPointerException，又称之为空指针异常，简称NPE。
程序试图指向内存中空位置引用(null)时，往往就会抛出这个异常。
最常见的情况就是，声明了一个对象，但并未初始化该对象，那么，若当在程序中试图引用该对象时，往往程序就会抛出这个异常，导致问题的出现。

# 处理NPE问题

## 简单处理

最常见的处理就是使用if-else语句，避免对空对象的使用：
```java
if(result!=null){  
    Map<String,String> data = result.getData();  
    if(data!=null){  
        String url = data.get("url");  
    }  
}
```
在上面的范例中，我们分别对result和data进行了null的判断，当出现null时，我们将不予执行对result和data的处理，避免程序引用null对象，抛出异常。

## Optional类

在java8中，为了优雅的解决这类问题，引入了Optional类。
```java
public final class Optional<T> {  
    /**  
     * Common instance for {@code empty()}.  
     */    
     private static final Optional<?> EMPTY = new Optional<>(null);  
  
    /**  
     * If non-null, the value; if null, indicates no value is present  
     * */    
		private final T value; 
	}
```

通过源码可以看到，Optional类中包含了非空时存储的泛型对象，和Null值时返回的EMPTY对象。
实际上，Optional的本质，就是在内部存储了一个真实的值。
并且，Optional通过实现了诸如`of()`、`empty()`、`isPresent()`、`ifPresent()`等方法，替代了繁杂的if-else的判空操作。

### empty、of、ofNullable

这三个函数将会构造出Optional的实例对象。在源码中，`Optional(T value)`，即Optional类的构造函数的可见性为private，不能由外部调用。
而这三个函数，则替代Optional的构造函数，返回一个我们所需要的Optional对象。
下面，是对这三个函数的示例：
```java
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
```

可以看到，`empty`函数直接返回了一个EMPTY对象。
`of`和`ofNullable`的差别则在于，当value值为null时，`of`会抛出NPE异常；而`ofNullable`函数不会抛出异常，而是返回一个EMPTY对象。

### isPresent与ifPresent
isPresent函数，顾名思义，即为判断Optional对象的value值是否为空，是则返回true，反之则返回false。
而ifPresent则是在value值不为空时，做一些操作。
示例如下：
```java
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
  
//ifPresent()方法 存在则操作  
optional.ifPresent(op -> {  
    if(op.getCode()==0){  
        System.out.println(op.getMessage());  
    }  
});
```

### orElse、orElseGet、orElseThrow
当我们在创建Optional对象时，如果需要一个默认值，则可以使用`orElse`、`orElseGet`函数。
这几个函数将会在Option构造的对象为空时进行调用，可以看到示例如下：
```java
//orElse与orElseGet方法:当optional对象为empty时,执行操作  
Result testOrElse = null;  
testOrElse = Optional.ofNullable(testOrElse).orElse(new Result().getResultDemo());  
testOrElse = Optional.ofNullable(testOrElse).orElseGet(()->new Result().getResultDemo());  
//orElseThrow:当optional对象为empty时,抛出异常  
testOrElse = Optional.ofNullable(testOrElse).orElseThrow();
```

### map和flatMap
当我们需要拿到Optional对象中下的值时，我们可以对其执行调用`map`函数。如果返回值不为null，map会创建一个包含其对应返回值的Optional对象；否则，会返回empty对象。
```java
//当我们需要拿到result对象中里面的data对象时,我们就可以用到map方法  
Optional<Map<String,String>> data = Optional.of(new Result().getResultDemo()).map(Result::getData);  
data.ifPresent(System.out::println);
```
而`flatMap`方法和`map`方法类似，区别在于传入方法的Lambda表达式的类型。
在`flatMap`方法中，Lambda表达式的返回值必须是一个Optional实例对象。





