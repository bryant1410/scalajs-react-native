package chandu0101.scalajs.rn

import chandu0101.scalajs.rn.ReactNativeComponentB.LifeCycle
import japgolly.scalajs.react._

import scala.scalajs.js.{Any => JAny, Dynamic, ThisFunction, UndefOr, undefined}

/**
 * Created by chandrasekharkode on 4/1/15.
 */
object ReactNativeComponentB {

  final class FnResults[R](aa: => R, bb: => R) {
    lazy val a = aa
    lazy val b = bb
  }

  final class FnComposer[R](m: FnResults[R] => R) {
    def apply[A](uf: UndefOr[A => R], g: A => R) =
      uf.fold(g)(f => a => m(new FnResults(f(a), g(a))))

    def apply[A, B](uf: UndefOr[(A, B) => R], g: (A, B) => R) =
      uf.fold(g)(f => (a, b) => m(new FnResults(f(a, b), g(a, b))))

    def apply[A, B, C](uf: UndefOr[(A, B, C) => R], g: (A, B, C) => R) =
      uf.fold(g)(f => (a, b, c) => m(new FnResults(f(a, b, c), g(a, b, c))))
  }

  val fcUnit = new FnComposer[Unit](r => {
    r.a;
    r.b
  })
  val fcEither = new FnComposer[Boolean](r => r.a || r.b)
  // ===================================================================================================================
  // Convenience

  /**
   * Create a component that always displays the same content, never needs to be redrawn, never needs vdom diffing.
   */
  def static(name: String, content: ReactElement) =
    ReactNativeComponentB[Unit](name)
      .stateless
      .noBackend
      .render(_ => content)
      .shouldComponentUpdate((_, _, _) => false)

  // ===================================================================================================================
  // Builder

  @inline def apply[Props](name: String) = new P[Props](name)

  implicit def defaultDomType[P, S, B](c: ReactNativeComponentB[P, S, B]) = c.domType[TopNode]

  implicit def defaultProps[P, S, B, N <: TopNode](c: ReactNativeComponentB[P, S, B]#AnchorN[N]) = c.propsRequired

  implicit def defaultDomTypeAndProps[P, S, B](c: ReactNativeComponentB[P, S, B]) = defaultProps(defaultDomType(c))

  // ===================================================================================================================
  final class P[Props] private[ReactNativeComponentB](name: String) {

    // getInitialState is how it's named in React
    def getInitialState[State](f: Props => State) = initialStateP(f)

    def initialStateP[State](f: Props => State) = new PS(name, f)

    def initialState[State](s: => State) = initialStateP(_ => s)

    def stateless = initialState(())

    def render(f: Props => ReactElement) = stateless.render((p, _) => f(p))

    def render(f: (Props, PropsChildren) => ReactElement) = stateless.render((p, c, _) => f(p, c))
  }

  // ===================================================================================================================
  final class PS[Props, State] private[ReactNativeComponentB](name: String, initF: Props => State) {

    def backend[Backend](f: BackendScope[Props, State] => Backend) = new PSB(name, initF, f)

    def noBackend = backend(_ => ())

    def render(f: (Props, State) => ReactElement) = noBackend.render((p, s, _) => f(p, s))

    def render(f: (Props, PropsChildren, State) => ReactElement) = noBackend.render((p, c, s, _) => f(p, c, s))

    def render(f: ComponentScopeU[Props, State, Unit] => ReactElement) = noBackend.render(f)

    def renderS(f: (ComponentScopeU[Props, State, Unit], Props, State) => ReactElement) = noBackend.renderS(f)
  }

  // ===================================================================================================================
  final class PSB[P, S, B] private[ReactNativeComponentB](name: String, initF: P => S, backF: BackendScope[P, S] => B) {

    def render(f: ComponentScopeU[P, S, B] => ReactElement): ReactNativeComponentB[P, S, B] =
      new ReactNativeComponentB(name, initF, backF, f,
        LifeCycle(undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined))

    def render(f: (P, S, B) => ReactElement): ReactNativeComponentB[P, S, B] =
      render(s => f(s.props, s.state, s.backend))

    def render(f: (P, PropsChildren, S, B) => ReactElement): ReactNativeComponentB[P, S, B] =
      render(s => f(s.props, s.propsChildren, s.state, s.backend))

    def renderS(f: (ComponentScopeU[P, S, B], P, S) => ReactElement): ReactNativeComponentB[P, S, B] =
      render(T => f(T, T.props, T.state))
  }

  // ===================================================================================================================
  case class LifeCycle[P, S, B](
                                 getDefaultProps: UndefOr[() => P],
                                 componentWillMount: UndefOr[ComponentScopeU[P, S, B] => Unit],
                                 componentDidMount: UndefOr[ComponentScopeM[P, S, B] => Unit],
                                 componentWillUnmount: UndefOr[ComponentScopeM[P, S, B] => Unit],
                                 componentWillUpdate: UndefOr[(ComponentScopeWU[P, S, B], P, S) => Unit],
                                 componentDidUpdate: UndefOr[(ComponentScopeM[P, S, B], P, S) => Unit],
                                 componentWillReceiveProps: UndefOr[(ComponentScopeM[P, S, B], P) => Unit],
                                 shouldComponentUpdate: UndefOr[(ComponentScopeM[P, S, B], P, S) => Boolean])

}

final class ReactNativeComponentB[P, S, B](val name: String,
                                           initF: P => S,
                                           backF: BackendScope[P, S] => B,
                                           rendF: ComponentScopeU[P, S, B] => ReactElement,
                                           lc: LifeCycle[P, S, B]) {

  def configure(fs: (ReactNativeComponentB[P, S, B] => ReactNativeComponentB[P, S, B])*): ReactNativeComponentB[P, S, B] =
    fs.foldLeft(this)((a, f) => f(a))

  @inline private implicit def lcmod(a: LifeCycle[P, S, B]): ReactNativeComponentB[P, S, B] =
    new ReactNativeComponentB(name, initF, backF, rendF, a)

  def componentWillMount(f: ComponentScopeU[P, S, B] => Unit): ReactNativeComponentB[P, S, B] =
    lc.copy(componentWillMount = ReactNativeComponentB.fcUnit(lc.componentWillMount, f))

  def componentDidMount(f: ComponentScopeM[P, S, B] => Unit): ReactNativeComponentB[P, S, B] =
    lc.copy(componentDidMount = ReactNativeComponentB.fcUnit(lc.componentDidMount, f))

  def componentWillUnmount(f: ComponentScopeM[P, S, B] => Unit): ReactNativeComponentB[P, S, B] =
    lc.copy(componentWillUnmount = ReactNativeComponentB.fcUnit(lc.componentWillUnmount, f))

  def componentWillUpdate(f: (ComponentScopeWU[P, S, B], P, S) => Unit): ReactNativeComponentB[P, S, B] =
    lc.copy(componentWillUpdate = ReactNativeComponentB.fcUnit(lc.componentWillUpdate, f))

  def componentDidUpdate(f: (ComponentScopeM[P, S, B], P, S) => Unit): ReactNativeComponentB[P, S, B] =
    lc.copy(componentDidUpdate = ReactNativeComponentB.fcUnit(lc.componentDidUpdate, f))

  def componentWillReceiveProps(f: (ComponentScopeM[P, S, B], P) => Unit): ReactNativeComponentB[P, S, B] =
    lc.copy(componentWillReceiveProps = ReactNativeComponentB.fcUnit(lc.componentWillReceiveProps, f))

  def shouldComponentUpdate(f: (ComponentScopeM[P, S, B], P, S) => Boolean): ReactNativeComponentB[P, S, B] =
    lc.copy(shouldComponentUpdate = ReactNativeComponentB.fcEither(lc.shouldComponentUpdate, f))

  // ===================================================================================================================
  def domType[N2 <: TopNode] = new AnchorN[N2]

  final class AnchorN[N <: TopNode] private[ReactNativeComponentB] {
    @inline private def builder[C](cc: ReactComponentCU[P, S, B, N] => C) = new Builder(cc)

    def propsRequired = builder(new ReactComponentC.ReqProps[P, S, B, N](_, undefined, undefined))

    def propsDefault(p: => P) = builder(new ReactComponentC.DefaultProps[P, S, B, N](_, undefined, undefined, () => p))

    def propsConst(p: => P) = builder(new ReactComponentC.ConstProps[P, S, B, N](_, undefined, undefined, () => p))

    def propsUnit(implicit ev: Unit =:= P) = propsConst(ev(()))

    def buildU(implicit ev: Unit =:= P) = propsUnit.build
  }

  final class Builder[C, N <: TopNode] private[ReactNativeComponentB](cc: ReactComponentCU[P, S, B, N] => C) {

    def buildSpec: ReactComponentSpec[P, S, B, N] = {
      val spec = Dynamic.literal(
        "displayName" -> name,
        "backend" -> 0,
        "render" -> (rendF: ThisFunction)
      )

      @inline def setFnPS[T, R](fn: UndefOr[(T, P, S) => R], name: String): Unit =
        fn.foreach { f =>
          val g = (t: T, p: WrapObj[P], s: WrapObj[S]) => f(t, p.v, s.v)
          spec.updateDynamic(name)(g: ThisFunction)
        }

      val componentWillMount2 = (t: ComponentScopeU[P, S, B]) => {
        val scopeB = t.asInstanceOf[BackendScope[P, S]]
        t.asInstanceOf[Dynamic].updateDynamic("backend")(backF(scopeB).asInstanceOf[JAny])
        lc.componentWillMount.foreach(g => g(t))
      }
      spec.updateDynamic("componentWillMount")(componentWillMount2: ThisFunction)

      val initStateFn: ComponentScopeU[P, S, B] => WrapObj[S] = scope => WrapObj(initF(scope.props))
      spec.updateDynamic("getInitialState")(initStateFn: ThisFunction)

      lc.getDefaultProps.foreach(f => spec.updateDynamic("getDefaultProps")(f: () => P))
      lc.componentWillUnmount.foreach(f => spec.updateDynamic("componentWillUnmount")(f: ThisFunction))
      lc.componentDidMount.foreach(f => spec.updateDynamic("componentDidMount")(f: ThisFunction))

      setFnPS(lc.componentWillUpdate, "componentWillUpdate")
      setFnPS(lc.componentDidUpdate, "componentDidUpdate")
      setFnPS(lc.shouldComponentUpdate, "shouldComponentUpdate")

      lc.componentWillReceiveProps.foreach { f =>
        val g = (t: ComponentScopeM[P, S, B], p: WrapObj[P]) => f(t, p.v)
        spec.updateDynamic("componentWillReceiveProps")(g: ThisFunction)
      }

      spec.asInstanceOf[ReactComponentSpec[P, S, B, N]]
    }

    def build: C =
      cc(ReactNative.createFactory(ReactNative.createClass(buildSpec)))

    /**
     * use this for root component of native app ( passed to AppRegistry.registerComponent(.. )
     * @return
     */
    def buildNative = ReactNative.createClass(buildSpec)

  }

}