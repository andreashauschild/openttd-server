import {Component, OnInit} from '@angular/core';
import SunsetTheme from 'highcharts/themes/dark-unica.js';
import * as Highcharts from 'highcharts';
import * as HighchartsStock from 'highcharts/highstock';
import {BackendWebsocketService} from './shared/services/backend-websocket.service';
import {Router} from '@angular/router';
import {AuthenticationService} from './shared/services/authentication.service';
import {SidebarLayoutModel} from './shared/ui/sidebar-layout/sidebar-layout.component';
import {Store} from '@ngrx/store';
import {selectAlerts} from './shared/store/selectors/app.selectors';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AppNotificationsComponent} from './shared/ui/app-notifications/app-notifications.component';
import {createAlert} from './shared/store/actions/app.actions';
import {ApplicationService} from './shared/services/application.service';

SunsetTheme(Highcharts);
SunsetTheme(HighchartsStock);

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  window: any;

  sidebarLayoutModel: SidebarLayoutModel = {
    entries: [
      {
        icon: 'dns',
        path: "/servers",
        title: "Servers"
      },
      {
        icon: 'settings',
        path: "/settings",
        title: "Settings"
      },
    ]
  }

  constructor(private app:ApplicationService, public auth: AuthenticationService, private router: Router, private backendWebsocketService: BackendWebsocketService) {
    this.window = window;
  }

  ngOnInit(): void {
    this.backendWebsocketService.connect();


  }


  test() {
    this.app.createErrorMessage("ERROR");
    this.app.createInfoMessage("ERROR",2000);
    this.app.createWarningMessage("ERROR","Exception handling request 36923a09-5ee0-480b-9c85-d58335e17536-1 to /api/auth/login: org.jboss.resteasy.spi.UnhandledException: java.lang.NullPointerException: Cannot invoke \"de.litexo.security.BasicAuth.getPassword()\" because \"auth\" is null\n" +
      "\tat org.jboss.resteasy.core.ExceptionHandler.handleApplicationException(ExceptionHandler.java:105)\n" +
      "\tat org.jboss.resteasy.core.ExceptionHandler.handleException(ExceptionHandler.java:359)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.writeException(SynchronousDispatcher.java:218)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:519)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.lambda$invoke$4(SynchronousDispatcher.java:261)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.lambda$preprocess$0(SynchronousDispatcher.java:161)\n" +
      "\tat org.jboss.resteasy.core.interception.jaxrs.PreMatchContainerRequestContext.filter(PreMatchContainerRequestContext.java:364)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.preprocess(SynchronousDispatcher.java:164)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:247)\n" +
      "\tat org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher.service(ServletContainerDispatcher.java:249)\n" +
      "\tat io.quarkus.resteasy.runtime.ResteasyFilter.doFilter(ResteasyFilter.java:35)\n" +
      "\tat io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
      "\tat io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
      "\tat de.litexo.AngularRouteFilter.doFilter(AngularRouteFilter.java:29)\n" +
      "\tat de.litexo.AngularRouteFilter_Subclass.doFilter$$superforward1(Unknown Source)\n" +
      "\tat de.litexo.AngularRouteFilter_Subclass$$function$$7.apply(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:54)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:41)\n" +
      "\tat io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:33)\n" +
      "\tat de.litexo.AngularRouteFilter_Subclass.doFilter(Unknown Source)\n" +
      "\tat io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
      "\tat io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
      "\tat io.undertow.servlet.handlers.FilterHandler.handleRequest(FilterHandler.java:84)\n" +
      "\tat io.undertow.servlet.handlers.security.ServletSecurityRoleHandler.handleRequest(ServletSecurityRoleHandler.java:63)\n" +
      "\tat io.undertow.servlet.handlers.ServletChain$1.handleRequest(ServletChain.java:68)\n" +
      "\tat io.undertow.servlet.handlers.ServletDispatchingHandler.handleRequest(ServletDispatchingHandler.java:36)\n" +
      "\tat io.undertow.servlet.handlers.RedirectDirHandler.handleRequest(RedirectDirHandler.java:67)\n" +
      "\tat io.undertow.servlet.handlers.security.SSLInformationAssociationHandler.handleRequest(SSLInformationAssociationHandler.java:133)\n" +
      "\tat io.undertow.servlet.handlers.security.ServletAuthenticationCallHandler.handleRequest(ServletAuthenticationCallHandler.java:57)\n" +
      "\tat io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
      "\tat io.undertow.security.handlers.AbstractConfidentialityHandler.handleRequest(AbstractConfidentialityHandler.java:46)\n" +
      "\tat io.undertow.servlet.handlers.security.ServletConfidentialityConstraintHandler.handleRequest(ServletConfidentialityConstraintHandler.java:65)\n" +
      "\tat io.undertow.security.handlers.AuthenticationMechanismsHandler.handleRequest(AuthenticationMechanismsHandler.java:60)\n" +
      "\tat io.undertow.servlet.handlers.security.CachedAuthenticatedSessionHandler.handleRequest(CachedAuthenticatedSessionHandler.java:77)\n" +
      "\tat io.undertow.security.handlers.NotificationReceiverHandler.handleRequest(NotificationReceiverHandler.java:50)\n" +
      "\tat io.undertow.security.handlers.AbstractSecurityContextAssociationHandler.handleRequest(AbstractSecurityContextAssociationHandler.java:43)\n" +
      "\tat io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
      "\tat io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler.handleFirstRequest(ServletInitialHandler.java:247)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler.access$100(ServletInitialHandler.java:56)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler$2.call(ServletInitialHandler.java:111)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler$2.call(ServletInitialHandler.java:108)\n" +
      "\tat io.undertow.servlet.core.ServletRequestContextThreadSetupAction$1.call(ServletRequestContextThreadSetupAction.java:48)\n" +
      "\tat io.undertow.servlet.core.ContextClassLoaderSetupAction$1.call(ContextClassLoaderSetupAction.java:43)\n" +
      "\tat io.quarkus.undertow.runtime.UndertowDeploymentRecorder$9$1.call(UndertowDeploymentRecorder.java:595)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler.dispatchRequest(ServletInitialHandler.java:227)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler.handleRequest(ServletInitialHandler.java:152)\n" +
      "\tat io.quarkus.undertow.runtime.UndertowDeploymentRecorder$1.handleRequest(UndertowDeploymentRecorder.java:120)\n" +
      "\tat io.undertow.server.Connectors.executeRootHandler(Connectors.java:284)\n" +
      "\tat io.undertow.server.DefaultExchangeHandler.handle(DefaultExchangeHandler.java:18)\n" +
      "\tat io.quarkus.undertow.runtime.UndertowDeploymentRecorder$5$1.run(UndertowDeploymentRecorder.java:417)\n" +
      "\tat java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:577)\n" +
      "\tat java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)\n" +
      "\tat io.quarkus.vertx.core.runtime.VertxCoreRecorder$14.runWith(VertxCoreRecorder.java:564)\n" +
      "\tat org.jboss.threads.EnhancedQueueExecutor$Task.run(EnhancedQueueExecutor.java:2449)\n" +
      "\tat org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1478)\n" +
      "\tat org.jboss.threads.DelegatingRunnable.run(DelegatingRunnable.java:29)\n" +
      "\tat org.jboss.threads.ThreadLocalResettingRunnable.run(ThreadLocalResettingRunnable.java:29)\n" +
      "\tat io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)\n" +
      "\tat java.base/java.lang.Thread.run(Thread.java:1589)\n" +
      "Caused by: java.lang.NullPointerException: Cannot invoke \"de.litexo.security.BasicAuth.getPassword()\" because \"auth\" is null\n" +
      "\tat de.litexo.security.SecurityService.login(SecurityService.java:45)\n" +
      "\tat de.litexo.security.SecurityService_Subclass.login$$superforward1(Unknown Source)\n" +
      "\tat de.litexo.security.SecurityService_Subclass$$function$$2.apply(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:54)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:41)\n" +
      "\tat io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:33)\n" +
      "\tat de.litexo.security.SecurityService_Subclass.login(Unknown Source)\n" +
      "\tat de.litexo.security.SecurityService_ClientProxy.login(Unknown Source)\n" +
      "\tat de.litexo.security.AuthResource.login(AuthResource.java:27)\n" +
      "\tat de.litexo.security.AuthResource_Subclass.login$$superforward1(Unknown Source)\n" +
      "\tat de.litexo.security.AuthResource_Subclass$$function$$1.apply(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:54)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:50)\n" +
      "\tat io.quarkus.security.runtime.interceptor.SecurityHandler.handle(SecurityHandler.java:47)\n" +
      "\tat io.quarkus.security.runtime.interceptor.SecurityHandler_Subclass.handle$$superforward1(Unknown Source)\n" +
      "\tat io.quarkus.security.runtime.interceptor.SecurityHandler_Subclass$$function$$2.apply(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:54)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:41)\n" +
      "\tat io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:33)\n" +
      "\tat io.quarkus.security.runtime.interceptor.SecurityHandler_Subclass.handle(Unknown Source)\n" +
      "\tat io.quarkus.security.runtime.interceptor.PermitAllInterceptor.intercept(PermitAllInterceptor.java:23)\n" +
      "\tat io.quarkus.security.runtime.interceptor.PermitAllInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:41)\n" +
      "\tat io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:33)\n" +
      "\tat de.litexo.security.AuthResource_Subclass.login(Unknown Source)\n" +
      "\tat java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)\n" +
      "\tat java.base/java.lang.reflect.Method.invoke(Method.java:578)\n" +
      "\tat org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:170)\n" +
      "\tat org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:130)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.internalInvokeOnTarget(ResourceMethodInvoker.java:660)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invokeOnTargetAfterFilter(ResourceMethodInvoker.java:524)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.lambda$invokeOnTarget$2(ResourceMethodInvoker.java:474)\n" +
      "\tat org.jboss.resteasy.core.interception.jaxrs.PreMatchContainerRequestContext.filter(PreMatchContainerRequestContext.java:364)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invokeOnTarget(ResourceMethodInvoker.java:476)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:434)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:408)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:69)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:492)\n" +
      "\t... 60 more");
    this.app.createErrorMessage("ERROR","" +
      "Exception handling request 36923a09-5ee0-480b-9c85-d58335e17536-1 to /api/auth/login: org.jboss.resteasy.spi.UnhandledException: java.lang.NullPointerException: Cannot invoke \"de.litexo.security.BasicAuth.getPassword()\" because \"auth\" is null\n" +
      "\tat org.jboss.resteasy.core.ExceptionHandler.handleApplicationException(ExceptionHandler.java:105)\n" +
      "\tat org.jboss.resteasy.core.ExceptionHandler.handleException(ExceptionHandler.java:359)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.writeException(SynchronousDispatcher.java:218)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:519)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.lambda$invoke$4(SynchronousDispatcher.java:261)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.lambda$preprocess$0(SynchronousDispatcher.java:161)\n" +
      "\tat org.jboss.resteasy.core.interception.jaxrs.PreMatchContainerRequestContext.filter(PreMatchContainerRequestContext.java:364)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.preprocess(SynchronousDispatcher.java:164)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:247)\n" +
      "\tat org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher.service(ServletContainerDispatcher.java:249)\n" +
      "\tat io.quarkus.resteasy.runtime.ResteasyFilter.doFilter(ResteasyFilter.java:35)\n" +
      "\tat io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
      "\tat io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
      "\tat de.litexo.AngularRouteFilter.doFilter(AngularRouteFilter.java:29)\n" +
      "\tat de.litexo.AngularRouteFilter_Subclass.doFilter$$superforward1(Unknown Source)\n" +
      "\tat de.litexo.AngularRouteFilter_Subclass$$function$$7.apply(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:54)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:41)\n" +
      "\tat io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:33)\n" +
      "\tat de.litexo.AngularRouteFilter_Subclass.doFilter(Unknown Source)\n" +
      "\tat io.undertow.servlet.core.ManagedFilter.doFilter(ManagedFilter.java:61)\n" +
      "\tat io.undertow.servlet.handlers.FilterHandler$FilterChainImpl.doFilter(FilterHandler.java:131)\n" +
      "\tat io.undertow.servlet.handlers.FilterHandler.handleRequest(FilterHandler.java:84)\n" +
      "\tat io.undertow.servlet.handlers.security.ServletSecurityRoleHandler.handleRequest(ServletSecurityRoleHandler.java:63)\n" +
      "\tat io.undertow.servlet.handlers.ServletChain$1.handleRequest(ServletChain.java:68)\n" +
      "\tat io.undertow.servlet.handlers.ServletDispatchingHandler.handleRequest(ServletDispatchingHandler.java:36)\n" +
      "\tat io.undertow.servlet.handlers.RedirectDirHandler.handleRequest(RedirectDirHandler.java:67)\n" +
      "\tat io.undertow.servlet.handlers.security.SSLInformationAssociationHandler.handleRequest(SSLInformationAssociationHandler.java:133)\n" +
      "\tat io.undertow.servlet.handlers.security.ServletAuthenticationCallHandler.handleRequest(ServletAuthenticationCallHandler.java:57)\n" +
      "\tat io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
      "\tat io.undertow.security.handlers.AbstractConfidentialityHandler.handleRequest(AbstractConfidentialityHandler.java:46)\n" +
      "\tat io.undertow.servlet.handlers.security.ServletConfidentialityConstraintHandler.handleRequest(ServletConfidentialityConstraintHandler.java:65)\n" +
      "\tat io.undertow.security.handlers.AuthenticationMechanismsHandler.handleRequest(AuthenticationMechanismsHandler.java:60)\n" +
      "\tat io.undertow.servlet.handlers.security.CachedAuthenticatedSessionHandler.handleRequest(CachedAuthenticatedSessionHandler.java:77)\n" +
      "\tat io.undertow.security.handlers.NotificationReceiverHandler.handleRequest(NotificationReceiverHandler.java:50)\n" +
      "\tat io.undertow.security.handlers.AbstractSecurityContextAssociationHandler.handleRequest(AbstractSecurityContextAssociationHandler.java:43)\n" +
      "\tat io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
      "\tat io.undertow.server.handlers.PredicateHandler.handleRequest(PredicateHandler.java:43)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler.handleFirstRequest(ServletInitialHandler.java:247)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler.access$100(ServletInitialHandler.java:56)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler$2.call(ServletInitialHandler.java:111)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler$2.call(ServletInitialHandler.java:108)\n" +
      "\tat io.undertow.servlet.core.ServletRequestContextThreadSetupAction$1.call(ServletRequestContextThreadSetupAction.java:48)\n" +
      "\tat io.undertow.servlet.core.ContextClassLoaderSetupAction$1.call(ContextClassLoaderSetupAction.java:43)\n" +
      "\tat io.quarkus.undertow.runtime.UndertowDeploymentRecorder$9$1.call(UndertowDeploymentRecorder.java:595)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler.dispatchRequest(ServletInitialHandler.java:227)\n" +
      "\tat io.undertow.servlet.handlers.ServletInitialHandler.handleRequest(ServletInitialHandler.java:152)\n" +
      "\tat io.quarkus.undertow.runtime.UndertowDeploymentRecorder$1.handleRequest(UndertowDeploymentRecorder.java:120)\n" +
      "\tat io.undertow.server.Connectors.executeRootHandler(Connectors.java:284)\n" +
      "\tat io.undertow.server.DefaultExchangeHandler.handle(DefaultExchangeHandler.java:18)\n" +
      "\tat io.quarkus.undertow.runtime.UndertowDeploymentRecorder$5$1.run(UndertowDeploymentRecorder.java:417)\n" +
      "\tat java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:577)\n" +
      "\tat java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)\n" +
      "\tat io.quarkus.vertx.core.runtime.VertxCoreRecorder$14.runWith(VertxCoreRecorder.java:564)\n" +
      "\tat org.jboss.threads.EnhancedQueueExecutor$Task.run(EnhancedQueueExecutor.java:2449)\n" +
      "\tat org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1478)\n" +
      "\tat org.jboss.threads.DelegatingRunnable.run(DelegatingRunnable.java:29)\n" +
      "\tat org.jboss.threads.ThreadLocalResettingRunnable.run(ThreadLocalResettingRunnable.java:29)\n" +
      "\tat io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)\n" +
      "\tat java.base/java.lang.Thread.run(Thread.java:1589)\n" +
      "Caused by: java.lang.NullPointerException: Cannot invoke \"de.litexo.security.BasicAuth.getPassword()\" because \"auth\" is null\n" +
      "\tat de.litexo.security.SecurityService.login(SecurityService.java:45)\n" +
      "\tat de.litexo.security.SecurityService_Subclass.login$$superforward1(Unknown Source)\n" +
      "\tat de.litexo.security.SecurityService_Subclass$$function$$2.apply(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:54)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:41)\n" +
      "\tat io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:33)\n" +
      "\tat de.litexo.security.SecurityService_Subclass.login(Unknown Source)\n" +
      "\tat de.litexo.security.SecurityService_ClientProxy.login(Unknown Source)\n" +
      "\tat de.litexo.security.AuthResource.login(AuthResource.java:27)\n" +
      "\tat de.litexo.security.AuthResource_Subclass.login$$superforward1(Unknown Source)\n" +
      "\tat de.litexo.security.AuthResource_Subclass$$function$$1.apply(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:54)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:50)\n" +
      "\tat io.quarkus.security.runtime.interceptor.SecurityHandler.handle(SecurityHandler.java:47)\n" +
      "\tat io.quarkus.security.runtime.interceptor.SecurityHandler_Subclass.handle$$superforward1(Unknown Source)\n" +
      "\tat io.quarkus.security.runtime.interceptor.SecurityHandler_Subclass$$function$$2.apply(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.proceed(AroundInvokeInvocationContext.java:54)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.proceed(InvocationInterceptor.java:62)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor.monitor(InvocationInterceptor.java:49)\n" +
      "\tat io.quarkus.arc.runtime.devconsole.InvocationInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:41)\n" +
      "\tat io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:33)\n" +
      "\tat io.quarkus.security.runtime.interceptor.SecurityHandler_Subclass.handle(Unknown Source)\n" +
      "\tat io.quarkus.security.runtime.interceptor.PermitAllInterceptor.intercept(PermitAllInterceptor.java:23)\n" +
      "\tat io.quarkus.security.runtime.interceptor.PermitAllInterceptor_Bean.intercept(Unknown Source)\n" +
      "\tat io.quarkus.arc.impl.InterceptorInvocation.invoke(InterceptorInvocation.java:42)\n" +
      "\tat io.quarkus.arc.impl.AroundInvokeInvocationContext.perform(AroundInvokeInvocationContext.java:41)\n" +
      "\tat io.quarkus.arc.impl.InvocationContexts.performAroundInvoke(InvocationContexts.java:33)\n" +
      "\tat de.litexo.security.AuthResource_Subclass.login(Unknown Source)\n" +
      "\tat java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)\n" +
      "\tat java.base/java.lang.reflect.Method.invoke(Method.java:578)\n" +
      "\tat org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:170)\n" +
      "\tat org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:130)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.internalInvokeOnTarget(ResourceMethodInvoker.java:660)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invokeOnTargetAfterFilter(ResourceMethodInvoker.java:524)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.lambda$invokeOnTarget$2(ResourceMethodInvoker.java:474)\n" +
      "\tat org.jboss.resteasy.core.interception.jaxrs.PreMatchContainerRequestContext.filter(PreMatchContainerRequestContext.java:364)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invokeOnTarget(ResourceMethodInvoker.java:476)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:434)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:408)\n" +
      "\tat org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:69)\n" +
      "\tat org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:492)\n" +
      "\t... 60 more");
  }
}
