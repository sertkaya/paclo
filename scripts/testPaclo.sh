#!/bin/bash


DATA_DIR=./data
PACLO_JAR=./paclo-0.0.1-SNAPSHOT.jar
RUNS=5

DATE_TIME=`date +"%F-%T"`
LOG_FILE=log-$DATE_TIME

trap "echo Test script interrupted, exiting.; echo Test interrupted. >> $LOG_FILE; exit;" SIGINT SIGTERM

echo `date` >> $LOG_FILE

for test_data in `cat $DATA_DIR/test.conf`
do
  echo === $test_data ===
  echo === $test_data === >> $LOG_FILE
  LOG_DIR=logs-$DATE_TIME/$test_data
  mkdir -p $LOG_DIR
  initialOntology=$DATA_DIR/$test_data/initialOntology.owl
  expertOntology=$DATA_DIR/$test_data/expertOntology.owl
  baseSet=$DATA_DIR/$test_data/baseSet
  echo "baseline"
  echo "*** baseline ***" >> $LOG_FILE
  java -jar $PACLO_JAR -ontology 0.9999 0.9999 $initialOntology $expertOntology $baseSet $LOG_DIR/result-zero.owl > $LOG_DIR/log_zero  2>>$LOG_DIR/error
  grep -o -E 'Axioms added: [0-9]+' $LOG_DIR/log_zero >> $LOG_FILE
  grep -o -E 'Macro recall: 0\.[0-9]+' $LOG_DIR/log_zero >> $LOG_FILE
  grep -o -E 'Micro recall: 0\.[0-9]+' $LOG_DIR/log_zero >> $LOG_FILE
  for epsilon in 0.1 0.01 0.001 0.0001 0.00001 # 0.0001 0.1 0.2 # 0.3
  do
    echo $epsilon
    echo  >> $LOG_FILE
    echo -n "*** epsilon: $epsilon" >> $LOG_FILE
    for delta in 0.001 # 0.0001 # 0.2 0.3 0.01
    do
      echo " / delta: $delta ***" >> $LOG_FILE
      total_axs=0
      total_macro_precision=0
      total_macro_recall=0
      total_micro_precision=0
      total_micro_recall=0
      total_time=0
      for run in `seq $RUNS`
      do
        echo $run
        # echo "run: $run" >> $LOG_FILE
        result_ontology=$LOG_DIR/result-run$run-e$epsilon-d$delta.owl
        output_file=$LOG_DIR/log-run$run-e$epsilon-d$delta
        java -jar $PACLO_JAR -ontology $epsilon $delta $initialOntology $expertOntology $baseSet $result_ontology > $output_file 2>>$LOG_DIR/error
        # grep -o -E 'Axioms added: [0-9]+' $output_file >> $LOG_FILE
        # grep -o -E 'Quality: [01]\.[0-9]+' $output_file >> $LOG_FILE
        # grep -o -E 'Execution time: [0-9]+' $output_file >> $LOG_FILE
        axs=`grep -o -E 'Axioms added: [0-9]+' $output_file | sed 's/Axioms added: //'`
        macro_precision=`grep -o -E 'Macro precision: [01]\.[0-9]+' $output_file | sed 's/Macro precision: //'`
        macro_recall=`grep -o -E 'Macro recall: [01]\.[0-9]+' $output_file | sed 's/Macro recall: //'`
        micro_precision=`grep -o -E 'Micro precision: [01]\.[0-9]+' $output_file | sed 's/Micro precision: //'`
        micro_recall=`grep -o -E 'Micro recall: [01]\.[0-9]+' $output_file | sed 's/Micro recall: //'`
        time=`grep -o -E 'Execution time: [0-9]+' $output_file | sed 's/Execution time: //'`
        total_axs=$( echo "$total_axs + $axs" | bc -l )
        total_macro_precision=$( echo "$total_macro_precision + $macro_precision" | bc -l )
        total_macro_recall=$( echo "$total_macro_recall + $macro_recall" | bc -l )
        total_micro_precision=$( echo "$total_micro_precision + $micro_precision" | bc -l )
        total_micro_recall=$( echo "$total_micro_recall + $micro_recall" | bc -l )
        total_time=$( echo "$total_time + $time" | bc -l )
      done
      echo "Average of $RUNS runs:" >> $LOG_FILE
      echo -n "Axioms: " >> $LOG_FILE
      echo "$total_axs / $RUNS" | bc -l >> $LOG_FILE
      echo -n "Macro precision: " >> $LOG_FILE
      echo "$total_macro_precision / $RUNS" | bc -l >> $LOG_FILE
      echo -n "Macro recall: " >> $LOG_FILE
      echo "$total_macro_recall / $RUNS" | bc -l >> $LOG_FILE
      echo -n "Micro precision: " >> $LOG_FILE
      echo "$total_micro_precision / $RUNS" | bc -l >> $LOG_FILE
      echo -n "Micro recall: " >> $LOG_FILE
      echo "$total_micro_recall / $RUNS" | bc -l >> $LOG_FILE
      echo -n "Time: " >> $LOG_FILE
      echo "$total_time / $RUNS" | bc -l >> $LOG_FILE
    done
  done
done
echo "Test finished" >> $LOG_FILE
