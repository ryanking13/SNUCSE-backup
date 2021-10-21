## 전처리 과정 설명

#### 1. 엑셀 파일을 csv 형식으로 변환
생기연 측에서 받은 ‘snu_data1.xlsx’ 파일의 info 탭을 분리해 csv형식으로 변환해 ‘snu_info1.csv’ 파일명으로 ‘./raw_data/’ 디렉토리에 저장한다.

마찬가지로 같은 위치에 label 탭을 분리해 ‘snu_label1.csv’ 파일명으로 저장한다. 

Data 탭은 센서들이 세 파일에 나뉘어 있기 때문에 ‘snu_data1.xlsx’ , ‘snu_data2.xlsx’, ‘snu_data3.xlsx’ 파일을 수동으로 합친 이후 저장해야 한다. 한 시트에 세 파일의 데이터를 모두 합친 후 csv형식으로 변환해 ‘snu_data1.csv’ 파일명으로 저장한다.

생기연 측에서 추가로 받은 ‘snu_data_new1.xlsx’, ‘snu_data_new2.xlsx’, ‘snu_data_new3.xlsx’ 세 파일에 대해서도 같은 작업을 수행해 ‘./raw_data/’ 디렉토리에 ‘snu_info2.csv’, ‘snu_label2.csv’, ‘snu_data2.csv’의 세 파일을 생성한다.


#### 2. Parser 스크립트를 이용해 csv파일의 데이터를 웨이퍼별, 스텝별로 파싱

‘./dataParser.py’ 스크립트를 실행시킨다. 명령어는 다음과 같다.

$ python3 dataParser.py

이 때 ‘./aDielist.txt’ 파일에는 파싱 과정에서 무시할 센서의 목록이 저장되어 있다. 이 파일의 내용을 수정함으로써 어떤 센서를 파싱 과정에서 1차적으로 제거할 것인지 선택할 수 있다.

스크립트를 실행하면 ‘./data/’ 디렉토리가 생성된다. 그 내부에 웨이퍼, 스텝별로 파싱된 데이터가 ‘WAFERNAME_stepX.txt’ 의 파일명 형식으로 저장된다.

양불 여부와 함께 저장되어 있는 웨이퍼 이름들의 목록은 ‘./data/’ 디렉토리 내부에 ‘waferList.txt’ 파일에 저장되어 있다. 현재 해당 파일에 저장되어 있는 센서들 목록은 전 구간에서 0의 값만을 가지는 센서들이다.


#### 3. 파싱된 데이터를 센서별로 다시한번 나누어 저장

‘./sensorSplitter.py’ 스크립트를 실행시킨다. 명령어는 다음과 같다.

$ python3 sensorSplitter.py

스크립트를 실행하면 ‘./data/’ 디렉토리 내부에 ‘./data/stepX/’ 형태로 총 25개 스텝에 대한 디렉토리가 생성된다.

그 내부에는 다시 한번 ./data/stepX/vYYY/’ 형태로 모든 센서들에 대한 디렉토리가 생성된다.

그 내부에 모든 웨이퍼들의 해당 step, 해당 센서에 대한 데이터만 ‘0_WAFERNAME.txt’ 혹은 ‘1_WAFERNAME.txt’ 파일명으로 저장된다.

파일명 앞의 0 또는 1은 해당 웨이퍼의 양불을 나타내는 것으로, 양품일 때 1이다.


#### 4. 센서별로 나누어진 데이터를 이용해 그래프 생성

‘./plotter.py’ 스크립트를 실행시킨다. 명령어는 다음과 같다.

$ python3 plotter.py

스크립트를 실행하면 단계 3. 에서 step, 센서별로 분리한 데이터를 이용해 각각에 대한 그래프가 PNG파일로 생성된다. 

‘./plot/’ 디렉토리가 자동으로 생성되고, 그 내부에 단계 3. 에서와 유사한 디렉토리 구조가 생성된다.

‘./plot/stepX/vYYY/0_WAFERNAME.png’ 파일명으로 그래프들이 저장된다.

#### 5. 웨이퍼 별로 대푯값 추출

‘./dataStatistic.py’ 스크립트를 실행시킨다. 명령어는 다음과 같다.

$ python3 dataStatistic.py

스크립트를 실행시키면 ‘./data/’ 디렉토리 내부에 ‘./data/stat/’ 디렉토리가 자동으로 생성되고, 그 내부에 ‘WAFERNAME_stepX.txt’ 파일명으로 각 웨이퍼별로 대푯값이 저장되어 있는 파일들이 생성된다.

대푯값으로는 min, max, average, std의 네가지 값을 계산해 저장한다.

#### 6. 추출된 대푯값으로 무의미한 센서 제거

‘./sensorReducer.py’ 스크립트를 실행시킨다. 명령어는 다음과 같다.

$ python3 sensorReducer.py

스크립트를 실행시키면 단계 5. 에서 추출한 대푯값들을 토대로 ‘./data/WAFERNAME_stepX.txt’ 에 저장되어있는 데이터에서 무의미한 센서를 제거한다.

결과 파일은 같은 디렉토리에 ‘WAFERNAME_stepX_reduced.txt’ 파일명으로 저장된다.

#### 7. 무작위로 train/test set 생성

‘./dataSampler.py’ 스크립트를 실행시킨다. 명령어는 다음과 같다.

$ python3 dataSampler.py

스크립트를 실행하면 몇 개의 양품/불량 웨이퍼를 train set에 포함할 것인지, test set에는 몇개를 포함할 것인지 입력을 요구하는 질문이 나온다.

적절히 입력하면 총 set의 개수를 물어보는 질문이 나온다. 

입력을 완료하면 해당 개수만큼 무작위로 선정된 wafer들의 목록이 ‘./data/’ 디렉토리에 ‘trainListX.txt’, ‘testListX.txt’ 파일명으로 저장된다. 



## 클래스 및 스크립트 설명

#### Database.py ( class Database )

웨이퍼 센서 데이터를 불러오고 각 모델에 입력할 수 있는 형태로 제공해주는 클래스, KNN과 RNN에서 사용

> __init__(train_path, train_answer_file, test_path, test_answer_file, sufix)
* train_path, test_path
	* 해당 위치에 각각 train용, test용 데이터가 위치해야 함
* train_answer_file, test_answer_file
	* 각각 train_path, test_path에서 사용할 데이터에 대한 인덱스 파일의 이름
	* 인덱스 파일은 WAFER_NAME WAFER INDEX 형태로 구성되어 있어야 함
* sufix
	* 인덱스 파일로부터 해당하는 데이터 파일 명을 정확하게 찾는 데 사용
	* WAFER_NAME + sufix + .txt 형태로 파일을 찾음

#### accuracy_measure.py
f1_score를 계산하는 함수가 포함되어 있음


#### KNN.py ( class KNN )
K-Nearest Neighbor 모델이 구현되어 있는 클래스

> __init__(k, distance_method, neighbor_method)
* k
	* KNN 에서의 K
* distance_method
	* 거리 계산 알고리즘
	* 종류 : ED, DTW, FastDTW, Eros, PCA_ED, PCA_DTW
* neighbor_method
 	* 이웃 판정 알고리즘
	* 종류 : SIMPLE, BORDA

#### LSTMSetting.py
LSTM network에 대한 여러가지 상수

#### LSTMnetwork.py ( class LSTMNetwork )
LSTM(RNN) network가 구현된 클래스

#### train_KNN.py
KNN 학습 및 테스트 스크립트

* Database 관련 변수
	* path : train_path, test_path 지정
	* train_answer, test_answer : train_answer_file, test_answer_file 지정
	* 현재는 sufix가 step_num 지정되어 있으며 데이터 파일 이름에 맞춰서 적절하게 수정하여 사용


* KNN 관련 변수
	* d_method : distance_method 지정
	* n_method : neighbor_method 지정


* start_train()
	* KNN class 내부에 time_step을 통일해주는 부분이 없으므로 해당 문제로 학습이 되지 않을 경우 cut 부분을 uncomment하면 됨 (start_test() 에서도 동일하게)


* start_test()
	* Eros, PCA_ 등의 distance_method를 사용하는 경우 테스트에 앞서 - KNN.eigenvalues_post_setup() - 을 실행해 줘야 함
	* 가능한 경우 차원 축소를 하도록 되어 있음 - KNN.reduce_dimensions() - 하지 않아도 동작은 하나 정확도가 떨어짐

#### train_LSTM.py
RNN 학습 및 테스트 스크립트

* Database 관련 변수
	* path, train_answer, test_answer : train_KNN과 동일


* start_train()
	* batch_size : 학습에 사용할 데이터의 batch 크기
	* epoch_size : 학습 횟수

#### naiveBayesSteps.py
각 스텝 별 센서 대표값을 이용한 Naive Bayesian Classifier를 구현함

실행시 dataSampler.py로 생성한 train/test set을 총 몇 쌍 사용할 것인지 입력 받음

무의미한 센서 제거 후 남은 42개의 센서에 대해 min, max, average, std를 './data/stat/WAFERNAME_stepX.txt' 파일에서 찾음

각 set별로 실행 결과를 'NB_accuracy.txt', 'NB_fmeasure.txt', 'NB_traintime.txt', 'NB_testtime.txt' 파일에 작성함

#### DBNslSteps.java
각 스텝 별 시간에 따른 센서 값을 이용한 Dynamic Bayesian Network를 구현함

각 스텝에서는 무의미한 센서 제거 후 남은 42개의 센서 중, std가 0인 센서를 추가로 제외하고 남은 센서들만을 활용

스텝 1, 21은 일부 웨이퍼에서 본 모델로 구현하기에 시간, 메모리의 제약이 있어서 포함하지 않음

* 컴파일과 실행
 
	* bayesserver 라이브러리의 .jar 파일은 ./libs에 저장되어있음

	* javac -cp ".:./libs/_*_:" DBNslSteps.java 로 컴파일하고, java -cp .:./libs/_*_: DBNslSteps 로 실행

	* 실행시 dataSampler.py로 생성한 train/test set을 총 몇 쌍 사용할 것인지 입력 받음

* 결과 해석

	* file : 각 set별로 실행 결과를 'DBN_accuracy.txt', 'DBN_fmeasure.txt', 'DBN_traintime.txt', 'DBN_testtime.txt' 파일에 작성함

	* console : 진행도를 확인하기 위해 현재 진행중인 set과 스텝에 대해 DataTable 생성, 구조 학습, 파라미터 학습 완료시 '~ complete for step X, set Y.' 의 형식으로 출력
