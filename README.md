# OCR
##  先把图片转为 “.tif” 文件
 [lang].[fontname].exp[num].tif
lang为语言名称，fontname为字体名称，num为序号
（多个tif文件用jTessBoxEditor合成tools->merge tif为一个.tif）
##	生成box文件
``` tesseract chi.normal.exp0.tif chi.normal.exp0 -l chi_sim batch.nochop makebox ```
##  使用jTessBoxEdito纠正

##	生成tr文件
```tesseract  chi.normal.exp0.tif chi.normal.exp0  nobatch box.train```
##	计算字符集
```unicharset_extractor chi.normal.exp0.box```
##	新建字体文件
font_properties
normal 0 0 0 0 0 
##	生成训练文件
```shapeclustering -F font_properties -U unicharset chi.normal.exp0.tr
mftraining -F font_properties -U unicharset -O unicharset chi.normal.exp0.tr
cntraining chi.normal.exp0.tr```

##	合并文件
```combine_tessdata normal.```



```echo normal 0 0 0 0 0>font_properties
tesseract  chi.normal.exp0.tif chi.normal.exp0  nobatch box.train
unicharset_extractor chi.normal.exp0.box
shapeclustering -F font_properties -U unicharset chi.normal.exp0.tr
mftraining -F font_properties -U unicharset -O unicharset chi.normal.exp0.tr
cntraining chi.normal.exp0.tr
rename normproto normal.normproto 
rename inttemp normal.inttemp 
rename pffmtable normal.pffmtable 
rename shapetable normal.shapetable
rename unicharset normal.unicharset
combine_tessdata normal.
echo. & pause```
