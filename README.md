# MEmetrics
Metrics on gene symbols in the paper from the field of Metabolic Engineering
190223 @author wcjung

This program searches titles and abtracts of academic papers finding symbols
of genes and proteins.

## Classes 

### PaperRecord class

대사공학 논문 개체를 나타내는 클래스 입니다.
This class contains a single entity of paper, in this project, entries 
provided in the raw file by the Uniprot.

 
### ArrayedPaperRecord class


### SerializePaperData class

논문 요약 정보 데이터를 직렬화(Serialize)된 파일로 저장합니다.
This class saves the the paper abstracts records from raw data files into
serialized java data file.


### ProteinRecord class

단백질/유전자 개체를 나타내는 클래스 입니다.
This class contains single entity of protein/gene, in this project, entry 
unit provided in the raw file by the Uniprot.


### ArrayedProteinRecord class


### ProteinKeywordAlteration class

UniProt 단백질, 유전자가 갖는 키워드 항목의 검색에 관용도가 필요한 것들을 찾아서 추가적인 
키워드를 갖는 리스트를 만들어 저장합니다.
This class filter keywords which need generosity for redundant spell or
punctuation, and add alternative keywords to new separated list. 


### SerializeProteinData class

단백질/유전자 데이터를 직렬화(Serialize)된 파일로 저장합니다.
This class saves the the protein-gene records from raw data files into
serialized java data file.


### ProteinKeywordItemize class

직렬화된 단백질/유전자 데이터 파일에서 해당 개체를 구분하는 단어를 찾아 해쉬맵에
저장합니다.
extract keywords into keyword hash map
key is the keyword string and value is a list of location with entry


### MatchHit class
 
단백질 유전자 항목을 구분하는 단어가 각 논문 요약에 등장하는지를 테이블에 매핑합니다. 
This class maps the appearance of match hit between the protein-gene entity
and paper abstract on a table structured data.


### PaperMatchRun class
 
 
### PatternComparator class


### MatchStatistics class

이 클래스는 단백질-논문 링크 검색결과 데이터를 바탕으로 기초적인 통계를 내어
텍스트파일로 저장합니다.
This class prints basic statistics of match results into text readable file
 
### DeserializeFile class

 
### CaseSense1gram class

검색된 1-gram 카워드 중에서 4자 이하인 것들에 대소문자 구별하는 규칙을 정해 필터링합니다.
This class filters keywords of 1-gram having different cases of spelling to 
those of UniProt-extracted keyword.


### KeywInKeyword class

검색된 2단어 이상 키워드 중에서 각 단어가 다른 1단어 키워드의 검색결과를 완전히 포함하는 경우를 
찾아 포함된 키워드를 필터링합니다.
This class filters keywords of 1-gram having match result which is entirely 
included in the result of other multi-gram keyword.


### letterSize1to2 class

검색된 1-gram 카워드 중에서 1-letter keywords를 필터링합니다.
This class filters out 1-letter keywords from the result.



## Execution

Code block below shows example bash commands for compiling and running the program 

'''bash

javac -d ./bin/ -cp ./bin/ -sourcepath ./src/ ./src/paperJavanise/*
javac -d ./bin/ -cp ./bin/ -sourcepath ./src/ ./src/meMatchMap/*
javac -d ./bin/ -cp ./bin/ -sourcepath ./src/ ./src/matchedKeywordFilter/*

java -Xmx32G -cp ./bin/ paperJavanise.SerializePaperData \
         '/home/data/wcjung/backup/backup_meMet_sherie.rsnz/paperRec2/' \
         './data/serializedPaper/2307040330.paper' \
         './data/serializedPaper/2307040330.paperIdreadable' 


java -Xmx32G -cp ./bin/ proteinJavanise.SerializeProteinData \
         '/home/data/wcjung/clean_copy/genebib_java/proteinRec/' \
         'uniprot_sprot.dat' \
         './data/serializedProtein/' \
         '2307040330.protein' 


java -Xmx32G -cp ./bin/ proteinJavanise.ProteinKeywordItemize \
         './data/serializedProtein/2307040330.protein' \
         './data/serializedKeyword/2307040330.readable' \
         './data/serializedKeyword/2307040330.keyword' 

java -Xmx32G -cp ./bin/ meMatchMap.MatchHit \
         './data/serializedKeyword/2307040330.keyword' \
         './data/serializedPaper/2307040330.paper' \
         "./data/matchResult/2307040330.testResult" \
         "./data/matchResult/2307040330.testReadable" # --test

java -Xmx32G -cp ./bin/ meMatchMap.KeywordCentralize \
         "./data/matchResult/2307040330.testResult" \
         "./data/matchResult/2307040330_KO.testResult" \
         "./data/matchResult/2307040330_KO.testReadable" 


java -Xmx32G -cp ./bin/ matchedKeywordFilter.CaseSense1gram \
         './data/serializedKeyword/2307040330.keyword' \
         "./data/matchResult/2307040330_KO.testResult" \
         "./data/matchResult/2307040330_KO.filtrant1Result" \
         "./data/matchResult/2307040330_KO.removed1Readable" 


java -Xmx32G -cp ./bin/ matchedKeywordFilter.letterSize1to2 \
         "./data/matchResult/2307040330_KO.filtrant1Result" \
         "./data/matchResult/2307040330_KO.filtrant2Result" \
         "./data/matchResult/2307040330_KO.removed2Readable" 


java -Xmx32G -cp ./bin/ matchedKeywordFilter.KeywInKeyword \
         './data/serializedKeyword/2307040330.keyword' \
         "./data/matchResult/2307040330_KO.filtrant2Result" \
         "./data/matchResult/2307040330_KO.filtrant3Result" \
         "./data/matchResult/2307040330_KO.filtrant3Readable" \
         "./data/matchResult/2307040330_KO.removed3Readable" 


java -Xmx32G -cp ./bin/ meMatchMap.ExportReadableResult \
         "./data/matchResult/2307040330_KO.filtrant3Result" \
         './data/serializedPaper/2307040330.paperIdreadable' \
         "./data/matchResult/2307040330_KO.keyword2paperID" \

'''
