//
//	Discovery
//	Created by mahj on 2010-01-22.
//
package sidewayscoding.model

import net.liftweb._
import mapper._
import http._
import SHtml._ 
import util._
import net.liftweb.common._
import sidewayscoding.lib.{SafeSave}
import xml.{Text}
import java.util.Date

class Discovery extends LongKeyedMapper[Discovery] with IdPK {
	
	def getSingleton = Discovery
	
	// primatives 
	object title extends MappedTextarea(this,600) {
		override def validations = valMinLen(1, "A title has to contain more than one char") _ :: Nil
	}
	object description extends MappedTextarea(this, 600){
		override def validations = valMinLen(1, "A description has to contain more than one char") _ :: Nil
	}
	object year extends MappedInt(this){
		
		def validateYear (y: Int) = {
			val date = new Date
			if (year > date.getYear+1900) List(FieldError(this, Text("Year has to be between 0 and 2009")))
			else List[FieldError]()
		}
				
		override def validations = validateYear _ :: Nil
	}
	object reference extends MappedPoliteString(this, 128)
	object field extends MappedLongForeignKey(this, Field)
	object imageName extends MappedString(this,256)
	object isIdle extends MappedBoolean(this)
	
	
	// relationships
	def image = ImageInfo.find(By(ImageInfo.imgName,this.imageName)) match {
		case Full(image) => Full(image)
		case _ => Empty
	}
	def sources = DiscoverySource.findAll(By(DiscoverySource.discovery,this.id)).map(_.source.obj.open_!)
	def deleteSources = DiscoverySource.findAll(By(DiscoverySource.discovery, this.id)).foreach( _.delete_! )
	
	
	def dependenciesWithComments = DiscoveryDependency.findAll(By(DiscoveryDependency.dependent,this.id)).map{ dependency => 
		(dependency.dependency.obj.open_!, dependency.comment.is)
	}
		
	def dependentsWithComments = DiscoveryDependency.findAll(By(DiscoveryDependency.dependency,this.id)).map{ dependent => 
		(dependent.dependent.obj.open_!, dependent.comment.is)
	}
	def dependencies = DiscoveryDependency.findAll(By(DiscoveryDependency.dependent,this.id)).map(_.dependency.obj.open_!)
	def dependents = DiscoveryDependency.findAll(By(DiscoveryDependency.dependency,this.id)).map(_.dependent.obj.open_!)
 		 
}
object Discovery extends Discovery with LongKeyedMetaMapper[Discovery] with SafeSave[Discovery] {
	
	private def deleteWithValidDBState(in: Discovery) = {
		DiscoverySource.findAll(By(DiscoverySource.discovery,in)).foreach{_.delete_!}
		DiscoveryDependency.deleteConnections(in)
	}
	
	override def beforeDelete = deleteWithValidDBState _ :: Nil	
}
