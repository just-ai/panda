package com.justai.cm.core.actions;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.justai.cm.core.domain.Cmp;
import com.justai.cm.utils.FileHelper;
import com.justai.cm.core.domain.ConfigMap;

import difflib.*;

public class Diff extends BaseAction {
   @Override
   protected void exec0(Cmp cmp) {
      FileHelper renderFolder = cmp.getZRenderFolder();
      FileHelper pullFolder = cmp.getZPullFolder();

      if (!settings.noRender) {
         actions.get("render").exec0(cmp);
      }
      //TODO: NoPull option
      actions.get("pull").exec0(cmp);

      for (ConfigMap map : cmp.getZComponent().getConfigMap()) {
         FileHelper source = renderFolder.child("config").child(map.getSource());
         FileHelper target = pullFolder.child(map.getSource());

         List<String> targetLines = target.file.isFile() ? target.readLines() : Collections.emptyList();
         List<String> sourceLines = source.file.isFile() ? source.readLines() : Collections.emptyList();

         List<Delta<String>> deltas = DiffUtils.diff(targetLines, sourceLines).getDeltas();
         if (deltas.isEmpty())
         {
            continue;
         }
         System.out.println(String.format("diff %s", source));
         for (Delta<String> delta : deltas) {
            int position = delta.getOriginal().getPosition();
            List<String> removed = new ArrayList<>();
            List<String> added = new ArrayList<>();
            switch (delta.getType()) {
               case CHANGE:
                  removed.addAll(delta.getOriginal().getLines());
                  added.addAll(delta.getRevised().getLines());
                  break;
               case DELETE:
                  removed.addAll(delta.getOriginal().getLines());
                  break;
               case INSERT:
                  added.addAll(delta.getRevised().getLines());
                  break;
               default:
                  throw new IllegalStateException();
           }
           this.print(position, removed, added);
         }
     }
   }
     
   private void print(int position, Collection<String> original, Collection<String> revised) {
      System.out.println(String.format("line %s", position));
      for (String entry : original) {
         System.out.println(String.format("<\t%s", entry));
      }
      if (!original.isEmpty() && !revised.isEmpty()){
         System.out.println("---");
      }
      for (String entry : revised) {
         System.out.println(String.format(">\t%s", entry));
      }
   }
}