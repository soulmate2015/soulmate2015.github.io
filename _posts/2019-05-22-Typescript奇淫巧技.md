---
layout: post
title: TS奇淫巧技
author: Tower
date: '2019-05-22 00:00:00'
category: Typescript
summary: TS使用过程中的一些技巧
thumbnail: 190522typescript.jpg
---

## 1. 复制类型和值

如果你想移动一个类，你可能会想要做以下事情：

```typescript
class Foo {}

const Bar = Foo

let bar: Bar // Error: 不能找到名称 'Bar'
```

这会得到一个错误，因为 const 仅仅是复制了 Foo 到一个变量声明空间，因此你无法把 Bar 当作一个类型声明使用。正确的方式是使用 import 关键字，请注意，如果你在使用 namespace 或者 modules，使用 import 是你唯一能用的方式：

```typescript
namespace importing {
  export class Foo {}
}

import Bar = importing.Foo
let bar: Bar // ok
```

这个 import 技巧，仅适合于类型和变量

---

## 2. 双重断言

类型断言，尽管我们已经证明了它并不是那么安全，但它也还是有用武之地。如下一个非常实用的例子所示，当使用者了解传入参数更具体的类型时，类型断言能按预期工作：

```typescript
function handler(event: Event) {
  const mouseEvent = event as MouseEvent
}
```

然而，如下例子中的代码将会报错，尽管使用者已经使用了类型断言：

```typescript
function handler(event: Event) {
  const element = event as HTMLElement // Error: 'Event' 和 'HTMLElement' 中的任何一个都不能赋值给另外一个
}
```

如果你仍然想使用那个类型，你可以使用双重断言。首先断言成兼容所有类型的 any，编译器将不会报错：

```typescript
function handler(event: Event) {
  const element = (event as any) as HTMLElement // ok
}
```

---

## 3. 类的混合

TypeScript (和 JavaScript) 类只能严格的单继承，因此你不能做：

```typescript
function handler(event: Event) {
  class User extends Tagged, Timestamped { // ERROR : 不能多重继承
    // ..
  }
}
```

从可重用组件构建类的另一种方式是通过基类来构建它们，这种方式称为混合。

这个主意是简单的，采用函数 B 接受一个类 A，并且返回一个带有新功能的类的方式来替代 A 类扩展 B 来获取 B 上的功能，前者中的 B 即是混合。


> 「混合」是一个函数：
>+ 传入一个构造函数
>+ 创建一个带有新功能，并且扩展构造函数的新类
>+ 返回这个新类

一个完整的例子:

```typescript
// 所有 mixins 都需要
type Constructor<T = {}> = new (...args: any[]) => T

/////////////
// mixins 例子
////////////

// 添加属性的混合例子
function TimesTamped<TBase extends Constructor>(Base: TBase) {
  return class extends Base {
    timestamp = Date.now()
  }
}

///////////
// 组合类
///////////

// 简单的类
class User {
  name = ''
}

// 添加 TimesTamped 的 User
const TimestampedUser = TimesTamped(User)

//////////
// 使用组合类
//////////

const timestampedUserExample = new TimestampedUser()
console.log(timestampedUserExample.timestamp) // new Date().getTime()
console.log(timestampedUserExample.__proto__.constructor.name) // User
```

### 创建一个构造函数

混合接受一个类，并且使用新功能扩展它。因此，我们需要定义构造函数的类型：

```typescript
type Constructor<T = {}> = new (...args: any[]) => T
```

### 扩展一个类并且返回它
```typescript
// 添加属性的混合例子
function TimesTamped<TBase extends Constructor>(Base: TBase) {
  return class extends Base {
    timestamp = Date.now()
  }
}
```

---

## 4. Reflect Metadata

### 基础

Reflect Metadata 是 ES7 的一个提案，它主要用来在声明的时候添加和读取元数据。TypeScript 在 1.5+ 的版本已经支持它，你只需要：

* `npm i reflect-metadata --save`
* 在 `tsconfig.json` 里配置 `emitDecoratorMetadata` 选项

Reflect Metadata 的API可以用于类或者类的属性上：

```typescript
// /node_modules/reflect-metadata/index.d.ts

declare global {
  namespace Reflect {

    function metadata(
      metadataKey: any,
      metadataValue: any
    ): {
      (target: Function): void;
      (target: Object, propertyKey: string | symbol): void;
    };

  }
}
```

`Reflect.metadata` 当作 `Decorator` 使用，当修饰类时，在类上添加元数据，当修饰类属性时，在类原型的属性上添加元数据:

```typescript
import 'reflect-metadata'

@Reflect.metadata('inClass', 'A')
class Test {
  @Reflect.metadata('inMethod', 'B')
  public hello(): string {
    return 'hello world';
  }
}

console.log(Reflect.getMetadata('inClass', Test)); // 'A'
console.log(Reflect.getMetadata('inMethod', new Test(), 'hello')); // 'B'
```

### 获取类型信息

譬如在 `vue-property-decorator` 6.1 及其以下版本中，通过使用 `Reflect.getMetadata` API，`Prop` Decorator 能获取属性类型传至 Vue，简要代码如下

```typescript
// 获取属性类型
function Prop(): PropertyDecorator {
  return (target, key: string) => {
    const type = Reflect.getMetadata('design:type', target, key)
    console.log(target, key)  // someClass {}, Aprop
    console.log(`${key} type: ${type.name}`)  // Aprop type: String
  }
}

// 获取参数类型
function ParamType(): PropertyDecorator {
  return (target, key: string) => {
    console.log(Reflect.getMetadataKeys(target, key))  // [ 'design:returntype', 'design:paramtypes', 'design:type' ]
    const types = Reflect.getMetadata('design:paramtypes', target, key)
    const paramtypes = (types || []).map(type => type.name)
    console.log(paramtypes)  // [ 'Number', 'Number' ]
  }
}

// 获取返回值类型
function ReturnType(): PropertyDecorator {
  return (target, key: string) => {
    const type = Reflect.getMetadata('design:returntype', target, key)
    console.log(type.name)  // Number
  }
}

class someClass {
  @Prop()
  public Aprop!: string

  @ParamType()
  @ReturnType()
  public add(a: number, b: number): number {
    return a+b
  }
}
```
TS通过反射内置了一些可获取的元数据类型：

   * `'design:type'`: 获取属性类型
   * `'design:paramtypes'`: 获取函数参数类型
   * `'design:returntype'`: 获取函数返回值类型

### 自定义 metadataKey

除能获取类型信息外，常用于自定义 `metadataKey`，并在合适的时机获取它的值:

```typescript
function classDecorator(): ClassDecorator {
  return target => {
    // 在类上定义元数据，key 为 `classMetaData`，value 为 `a`
    Reflect.defineMetadata('classMetaData', 'a', target);
  };
}

function methodDecorator(): MethodDecorator {
  return (target, key, descriptor) => {
    // 在类的原型属性 'someMethod' 上定义元数据，key 为 `methodMetaData`，value 为 `b`
    Reflect.defineMetadata('methodMetaData', 'b', target, key);
  };
}

@classDecorator()
class SomeClass {
  @methodDecorator()
  someMethod() {}
}

Reflect.getMetadata('classMetaData', SomeClass); // 'a'
Reflect.getMetadata('methodMetaData', new SomeClass(), 'someMethod'); // 'b'
```

### 元数据应用-依赖注入

一个简单版本的例子:

```typescript
function Injectable(): ClassDecorator {
  return target => {}
}

class OtherClass {
  a = 1
}

@Injectable()
class OneClass {
  constructor(public readonly otherService: OtherService) {}

  testMethod() {
    console.log(this.otherService.a)
  }
}

type Constructor<T = any> = new (...args[]) => T

const Factory = <T>(target: Constructor<T>): T => {
  // 注入所有服务
  const providers = Reflect.getMetadata('design:paramtypes', target) // [OtherService]
  // 实例化所有服务
  const args = providers.map((provider: Constructor) => new provider())
  return new target(...args)
}

// IOC实例化
Factory(OneClass)

Factory(OneClass).testMethod()  // 1
```

### 元数据应用-路由绑定

IOC框架类Spring路由写法:
```typescript
@Controller('/test')
class SomeClass {
  @Get('/a')
  someGetMethod() {
    return 'hello world';
  }

  @Post('/b')
  somePostMethod() {}
}
```

这些 `Decorator` 也是基于 `Reflect Metadata` 实现，这次，我们将 `metadataKey` 定义在 `descriptor` 的 `value` 上:

```typescript
const METHOD_METADATA = 'method'
const PATH_METADATA = 'path'

const createMappingDecorator = (method: string) => (path: string): MethodDecorator => {
  return (target, propertyKey, descriptor) => {
    Reflect.defineMetadata(PATH_METADATA, path, descriptor.value)
    Reflect.defineMetadata(METHOD_METADATA, method, descriptor.value)
  }
}

const Get = createMappingDecorator('GET')
const Post = createMappingDecorator('POST')
```

接着，创建一个函数，映射出 route:

```typescript
function mapRoute(instance: Object) {
  const prototype = Object.getPrototypeOf(instance)

  // 筛选出类的 methodName
  const methodNames = Object.getOwnPropertyNames(prototype).filter(item => {
    return item !== 'constructor' && typeof prototype[item] === 'function'
  })

  return methodNames.map(methodName => {
    const fn = prototype[methodName]

    // 取出定义的 metadata
    const route = Reflect.getMetadata(PATH_METADATA, fn)
    const method = Reflect.getMetadata(METHOD_METADATA, fn)
    return {
      route,
      method,
      fn,
      methodName
    }
  })
}
```

我们可以得到一些有用的信息:

```typescript
Reflect.getMetadata(PATH_METADATA, SomeClass); // '/test'

mapRoute(new SomeClass());

/**
 * [{
 *    route: '/a',
 *    method: 'GET',
 *    fn: someGetMethod() { ... },
 *    methodName: 'someGetMethod'
 *  },{
 *    route: '/b',
 *    method: 'POST',
 *    fn: somePostMethod() { ... },
 *    methodName: 'somePostMethod'
 * }]
 *
 */
```

最后，只需把 `route` 相关信息绑在 `express` 或者 `koa` 上就 ok 了
