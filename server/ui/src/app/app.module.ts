import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {GoogleChartsModule} from 'angular-google-charts';
import {HttpClientModule} from '@angular/common/http';
import {HighchartsChartModule} from 'highcharts-angular';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatRadioModule} from '@angular/material/radio';
import {MatMenuModule} from '@angular/material/menu';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {StoreModule} from '@ngrx/store';
import {reducers, metaReducers} from './store/reducers';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {environment} from '../environments/environment';
import {EffectsModule} from '@ngrx/effects';
import {AppEffects} from './store/effects/app.effects';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {processUpdateEvent} from './store/actions/app.actions';
import {ManagementViewComponent} from './views/management-view/management-view.component';
import {NgTerminalModule} from 'ng-terminal';
import {TerminalComponent} from './components/terminal/terminal.component';
import {
  OpenttdProcessTerminalComponent
} from './components/openttd-process-terminal/openttd-process-terminal.component';
import {MatSelectModule} from "@angular/material/select";
import {OppttdServerTableComponent} from './components/oppttd-server-table/oppttd-server-table.component';
import {ServerFileSelectComponent} from './components/server-file-select/server-file-select.component';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {CreateServerDialogComponent} from './views/management-view/create-server-dialog/create-server-dialog.component';
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {FileUploadDialogComponent} from './components/file-upload-dialog/file-upload-dialog.component';
import {JustUploadModule} from "@andreashauschild/just-upload";
import { LoginViewComponent } from './views/login-view/login-view.component';

@NgModule({
  declarations: [
    AppComponent,
    ManagementViewComponent,
    TerminalComponent,
    OpenttdProcessTerminalComponent,
    OppttdServerTableComponent,
    ServerFileSelectComponent,
    CreateServerDialogComponent,
    FileUploadDialogComponent,
    LoginViewComponent
  ],
  imports: [
    NgTerminalModule,
    GoogleChartsModule,
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    HighchartsChartModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatSlideToggleModule,
    MatRadioModule,
    MatTooltipModule,
    MatTableModule,
    MatDialogModule,
    MatIconModule,
    MatMenuModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatTableModule,
    MatIconModule,
    EffectsModule.forRoot([AppEffects]),
    StoreModule.forRoot(reducers, {metaReducers}),
    !environment.production ? StoreDevtoolsModule.instrument(
      // {
      //   actionsBlocklist: [
      //     //terminalUpdateEvent.name
      //   ]
      // }
    ) : [],
    MatButtonModule,
    MatSelectModule,
    FormsModule,
    MatAutocompleteModule,
    JustUploadModule,

  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
