class C1<effect E>{}

class C2<region R, effect E> extends C1<reads R effect E>{}
