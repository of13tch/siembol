import { Component, ViewChild } from '@angular/core';
import { MatInput } from '@angular/material/input';
import { FieldType } from '@ngx-formly/material/form-field';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'formly-field-mat-textarea',
  template: `
        <textarea 
          class="text-area" 
          highlight 
          matInput          
          cdkTextareaAutosize 
          spellcheck="false"
          [class.hide-text]="true"
          [id]="id"
          [name]="to.title"
          [readonly]="to.readonly"
          [formControl]="formControl"
          [errorStateMatcher]="errorStateMatcher"
          [formlyAttributes]="field"
          [placeholder]="to.placeholder"
          [tabindex]="to.tabindex || 0"
          [readonly]="to.readonly"
          >
        </textarea>
        <div 
          class="highlighted-overlay"  
          [innerHtml]="value | highlightVariables">
        </div>
  `,
  styles: [`
    .text-area {
        resize: none;
        line-height: normal;
        overflow: hidden;
        margin: 0;
    }

    .hide-text {
        -webkit-text-fill-color: transparent;
    }

    .show-overlay {
        display: block;
        width: 100%;
        overflow-wrap: break-word;
        white-space: pre-wrap;
    }

    .highlighted-overlay {
        position: absolute;
        line-height: normal;
        top: 5.5px;
        left: 0;
        z-index: 10; 
    }

    ::ng-deep .mat-input-element {
        position: relative;
        z-index: 20;
    }
  `],
})
export class TextAreaTypeComponent extends FieldType {
  @ViewChild(MatInput, { static: true }) formFieldControl!: MatInput;
}
