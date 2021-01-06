import Utils.{calcLB, calcUB, isCellFullyDominated, isPointDominated}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Skyline {

  def computeSkyline(points:RDD[PointInCell]): Iterable[PointInCell]={
    val spark = SparkSession.builder().getOrCreate()
    val partByCell = points.groupBy(x=>x.cell)
    val prunedCells = pruneDominatedCells(partByCell.map(x=>x._1).collect().toBuffer)

    val broadcastPrunedCells = spark.sparkContext.broadcast(prunedCells)

    val prunedRDD = partByCell
      .filter(x=>broadcastPrunedCells.value.contains(x._1))
      .map(x=>(x._1,pruneDominatedPoints(x._2)))
      .flatMap(x=>x._2)

    val skyline = pruneDominatedPoints(prunedRDD.collect())
    skyline

  }

  def pruneDominatedPoints(points: Iterable[PointInCell]): Iterable[PointInCell] ={
    var pr = points.toBuffer
    val toPrune = new ArrayBuffer[Int]()
    for(i <- pr.indices)
      for(j <- pr.indices){
        if(i!=j && isPointDominated(pr(j).point,pr(i).point)){
          toPrune.append(j)
        }
      }
    pr = pr.zipWithIndex.filter(x=> !toPrune.contains(x._2)).map(x => x._1)
    pr
  }


  def pruneDominatedCells(input:mutable.Buffer[Cell]):  mutable.Buffer[Cell] ={
    var pr = input
    val toPrune = new ArrayBuffer[Int]()
    for(i <- pr.indices)
      for(j <- pr.indices){
        if(i!=j && isCellFullyDominated(pr(j),pr(i))){
          toPrune.append(j)
        }
      }
    pr = pr.zipWithIndex.filter(x=> !toPrune.contains(x._2)).map(x => x._1)
    pr
  }

}
