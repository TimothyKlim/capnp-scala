package org.katis.capnproto.runtime

class MessageBuilder(val arena: BuilderArena = new BuilderArena(BuilderArena.SUGGESTED_FIRST_SEGMENT_WORDS, BuilderArena.SUGGESTED_ALLOCATION_STRATEGY)) {

  def this(firstSegmentWords: Int) {
    this(new BuilderArena(firstSegmentWords, BuilderArena.SUGGESTED_ALLOCATION_STRATEGY))
  }

  def this(firstSegmentWords: Int, allocationStrategy: BuilderArena.AllocationStrategy.Value) {
    this(new BuilderArena(firstSegmentWords, allocationStrategy))
  }

  private def internalRoot: AnyPointer.Builder = {
    val rootSegment = this.arena.segments.get(0)
    if (rootSegment.currentSize() == 0) {
      val location = rootSegment.allocate(1)
      if (location == SegmentBuilder.FAILED_ALLOCATION) {
        throw new Error("could not allocate root pointer")
      }
      if (location != 0) {
        throw new Error("First allocated word of new segment was not at offset 0")
      }
      new AnyPointer.Builder(rootSegment, location)
    } else {
      new AnyPointer.Builder(rootSegment, 0)
    }
  }

  def getRoot[T <: PointerFamily : FromPointer]: T#Builder = this.internalRoot.getAs[T]

  def setRoot[T <: PointerFamily : SetPointerBuilder](reader: T#Reader) {
    internalRoot.setAs[T](reader)
  }

  def initRoot[T <: PointerFamily : FromPointer]: T#Builder = internalRoot.initAs[T]

  def getSegmentsForOutput(): Array[java.nio.ByteBuffer] = this.arena.getSegmentsForOutput
}
